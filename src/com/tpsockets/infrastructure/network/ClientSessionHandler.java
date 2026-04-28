package com.tpsockets.infrastructure.network;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.shared.Config;
import com.tpsockets.shared.logging.AppLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.regex.Pattern;

public class ClientSessionHandler implements Runnable {

  private final Socket socket;
  private final MessageProcessor messageProcessor;
  private final AppLogger logger;
  private final ClientBroadcaster clientBroadcaster;
  private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

  public ClientSessionHandler(
      Socket socket,
      MessageProcessor messageProcessor,
      AppLogger logger,
      ClientBroadcaster clientBroadcaster) {

    this.socket = Objects.requireNonNull(socket, "Socket no puede ser null");
    this.messageProcessor = Objects.requireNonNull(messageProcessor, "MessageProcessor no puede ser null");
    this.logger = Objects.requireNonNull(logger, "Logger no puede ser null");
    this.clientBroadcaster = Objects.requireNonNull(clientBroadcaster, "ClientBroadcaster no puede ser null");
  }

  @Override
  public void run() {
    String remoteAddress = socket.getRemoteSocketAddress().toString();

    String clientId = null;
    PrintWriter writer = null;

    try (socket; var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      writer = new PrintWriter(socket.getOutputStream(), true);
      clientId = resolveAndRegisterClientId(reader, writer);

      if (clientId == null) {
        return;
      }

      String clientInfo = clientId + " (" + remoteAddress + ")";
      logger.logConnection(clientInfo);

      String line;
      while ((line = reader.readLine()) != null) {
        logger.logReceived(line);

        String response = messageProcessor.process(line);
        logger.logSent(response);
        clientBroadcaster.println(writer, response);

        if (line.trim().equalsIgnoreCase(Config.EXIT_COMMAND)) {
          break;
        }
      }
    } catch (IOException e) {
      logger.logError("Error en sesión con " + remoteAddress + ": " + e.getMessage());
    } finally {
      if (clientId != null) {
        clientBroadcaster.unregister(clientId);
        logger.logDisconnection(clientId + " (" + remoteAddress + ")");
      } else {
        logger.logDisconnection(remoteAddress);
      }
    }
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
