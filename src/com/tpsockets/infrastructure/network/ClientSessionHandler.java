package com.tpsockets.infrastructure.network;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.domain.bet.BetCoordinator;
import com.tpsockets.domain.bet.BetSelectionState;
import com.tpsockets.shared.Config;
import com.tpsockets.shared.logging.AppLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.regex.Pattern;

public class ClientSessionHandler implements Runnable {

  private final Socket socket;
  private final MessageProcessor messageProcessor;
  private final AppLogger logger;
  private final ClientBroadcaster clientBroadcaster;
  private final BetCoordinator betCoordinator;
  private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

  public ClientSessionHandler(
      Socket socket,
      MessageProcessor messageProcessor,
      AppLogger logger,
      ClientBroadcaster clientBroadcaster,
      BetCoordinator betCoordinator) {

    this.socket = Objects.requireNonNull(socket, "Socket no puede ser null");
    this.messageProcessor = Objects.requireNonNull(messageProcessor, "MessageProcessor no puede ser null");
    this.logger = Objects.requireNonNull(logger, "Logger no puede ser null");
    this.clientBroadcaster = Objects.requireNonNull(clientBroadcaster, "ClientBroadcaster no puede ser null");
    this.betCoordinator = Objects.requireNonNull(betCoordinator, "BetCoordinator no puede ser null");
  }

  @Override
  public void run() {
    String remoteAddress = socket.getRemoteSocketAddress().toString();

    String clientId = null;
    PrintWriter writer = null;
    BetSelectionState betSelectionState = new BetSelectionState();

    try (socket; var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      writer = new PrintWriter(socket.getOutputStream(), true);
      logger.logInfo("Iniciando handshake de cliente: " + remoteAddress);
      clientId = resolveAndRegisterClientId(reader, writer);

      if (clientId == null) {
        logger.logInfo("Cliente cerró conexión antes de registrar ID: " + remoteAddress);
        return;
      }

      String clientInfo = clientId + " (" + remoteAddress + ")";
      logger.logConnection(clientInfo);

      String line;
      while ((line = reader.readLine()) != null) {
        logger.logReceived(line);

        String bettingResponse = handleBetFlow(line, clientId, betSelectionState);

        if (bettingResponse != null) {
          logger.logSent(bettingResponse);
          clientBroadcaster.println(writer, bettingResponse);

          if (line.trim().equalsIgnoreCase(Config.BET_CANCEL_COMMAND)) {
            continue;
          }

          if (betSelectionState.step() != BetSelectionState.Step.IDLE) {
            continue;
          }
        }

        String response = messageProcessor.process(line);
        logger.logSent(response);
        clientBroadcaster.println(writer, response);

        if (line.trim().equalsIgnoreCase(Config.EXIT_COMMAND)) {
          break;
        }
      }
    } catch (SocketException e) {
      if (clientId == null) {
        logger.logInfo("Conexión abortada durante handshake: " + remoteAddress + " (" + e.getMessage() + ")");
      } else {
        logger.logError("Conexión reseteada para " + clientId + " (" + remoteAddress + "): " + e.getMessage());
      }
    } catch (IOException e) {
      logger.logError("Error en sesión con " + remoteAddress + ": " + e.getMessage());
    } catch (RuntimeException e) {
      logger.logError("Error inesperado en sesión con " + remoteAddress + ": " + e.getMessage());
    } finally {
      if (clientId != null) {
        clientBroadcaster.unregister(clientId);
        logger.logDisconnection(clientId + " (" + remoteAddress + ")");
      } else {
        logger.logDisconnection(remoteAddress);
      }
    }
  }

  private String handleBetFlow(String line, String clientId, BetSelectionState betSelectionState) {
    String trimmedLine = line.trim();

    if (trimmedLine.equalsIgnoreCase(Config.EXIT_COMMAND)) {
      betSelectionState.reset();
      return null;
    }

    if (betSelectionState.step() == BetSelectionState.Step.IDLE
        && trimmedLine.equalsIgnoreCase(Config.BET_COMMAND)) {
      betSelectionState.startSelection();
      return betCoordinator.renderMatchList() + "\n" + Config.BET_PROMPT_SELECT_MATCH;
    }

    if (betSelectionState.step() == BetSelectionState.Step.SELECTING_MATCH) {
      if (trimmedLine.equalsIgnoreCase(Config.BET_CANCEL_COMMAND)) {
        betSelectionState.reset();
        return "BET cancelado.";
      }

      int matchId;
      try {
        matchId = Integer.parseInt(trimmedLine);
      } catch (NumberFormatException e) {
        return "Selección inválida. Ingrese un número de partido o CANCELAR.";
      }

      if (!betCoordinator.isValidMatchId(matchId)) {
        return "Partido inexistente. Seleccione un número válido o CANCELAR.";
      }

      betSelectionState.enterRoom(matchId);
      return "Te uniste al espacio de apuestas del partido #" + matchId + ".\n" + Config.BET_PROMPT_PLACE_BET;
    }

    if (betSelectionState.step() == BetSelectionState.Step.PLACING_BET) {
      if (trimmedLine.equalsIgnoreCase(Config.BET_CANCEL_COMMAND)) {
        betSelectionState.reset();
        return "Saliste del espacio de apuestas.";
      }

      String response = betCoordinator.placeBet(betSelectionState.selectedMatchId(), clientId, trimmedLine);

      if (response.startsWith("Apuesta registrada")) {
        return response + "\n" + Config.BET_PROMPT_PLACE_BET;
      }

      return response;
    }

    return null;
  }

  private String resolveAndRegisterClientId(BufferedReader reader, PrintWriter writer) throws IOException {
    while (true) {
      clientBroadcaster.println(writer, Config.CLIENT_ID_PROMPT);

      String candidate = reader.readLine();

      if (candidate == null) {
        return null;
      }

      String trimmedCandidate = candidate.trim();
      boolean isClientIdValid = CLIENT_ID_PATTERN.matcher(trimmedCandidate).matches();

      if (!isClientIdValid) {
        clientBroadcaster.println(writer, Config.CLIENT_ID_INVALID_MESSAGE);
        continue;
      }

      boolean registered = clientBroadcaster.register(trimmedCandidate, writer);

      if (!registered) {
        clientBroadcaster.println(writer, Config.CLIENT_ID_IN_USE_MESSAGE);
        continue;
      }

      clientBroadcaster.println(writer, Config.CLIENT_ID_ASSIGNED_PREFIX + trimmedCandidate);

      return trimmedCandidate;
    }
  }
}
