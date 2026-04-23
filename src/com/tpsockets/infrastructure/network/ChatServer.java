package com.tpsockets.infrastructure.network;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.infrastructure.console.ConsoleLogger;
import com.tpsockets.shared.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Objects;

public class ChatServer {

  private final int port;
  private final MessageProcessor messageProcessor;
  private final ConsoleLogger consoleLogger;
  private final ClientBroadcaster clientBroadcaster;

  public ChatServer(
      int port,
      MessageProcessor messageProcessor,
      ConsoleLogger consoleLogger,
      ClientBroadcaster clientBroadcaster) {

    boolean isPortValid = port > 0 && port <= 65535;

    if (!isPortValid) {
      throw new IllegalArgumentException("Puerto inválido: " + port);
    }

    Objects.requireNonNull(messageProcessor, "MessageProcessor no puede ser null");
    Objects.requireNonNull(consoleLogger, "ConsoleLogger no puede ser null");
    Objects.requireNonNull(clientBroadcaster, "ClientBroadcaster no puede ser null");

    this.port = port;
    this.messageProcessor = messageProcessor;
    this.consoleLogger = consoleLogger;
    this.clientBroadcaster = clientBroadcaster;
  }

  public void start() {
    startServerOperatorConsole();

    try (var serverSocket = new java.net.ServerSocket(port)) {
      consoleLogger.logInfo("Servidor iniciado en el puerto " + port);

      while (true) {
        var clientSocket = serverSocket.accept();
        consoleLogger.logInfo("Cliente aceptado: " + clientSocket.getRemoteSocketAddress());

        var handler =
            new ClientSessionHandler(clientSocket, messageProcessor, consoleLogger, clientBroadcaster);
        new Thread(handler).start();
      }
    } catch (java.io.IOException e) {
      consoleLogger.logError("Error en el servidor: " + e.getMessage());
    }
  }

  private void startServerOperatorConsole() {
    Thread operatorThread =
        new Thread(
            () -> {
              try {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                String line;

                while ((line = consoleReader.readLine()) != null) {
                  String trimmedLine = line.trim();
                  String normalizedLine = trimmedLine.toUpperCase(Locale.ROOT);
                  boolean isBroadcastCommand = normalizedLine.equals(Config.SERVER_BROADCAST_COMMAND);
                  boolean isBroadcastWithMessage =
                      normalizedLine.startsWith(Config.SERVER_BROADCAST_COMMAND + " ");

                  if (!isBroadcastCommand && !isBroadcastWithMessage) {
                    continue;
                  }

                  processBroadcastCommand(trimmedLine);
                }
              } catch (IOException e) {
                consoleLogger.logError("Error leyendo consola de servidor: " + e.getMessage());
              }
            });

    operatorThread.setDaemon(true);
    operatorThread.start();
  }

  private void processBroadcastCommand(String trimmedLine) {
    String payloadCandidate = trimmedLine.substring(Config.SERVER_BROADCAST_COMMAND.length()).trim();

    if (payloadCandidate.isEmpty()) {
      consoleLogger.logInfo(Config.SERVER_BROADCAST_USAGE);
      return;
    }

    String[] parts = payloadCandidate.split("\\s+", 2);
    String firstToken = parts[0];
    boolean hasSecondPart = parts.length > 1;
    String remainingText = hasSecondPart ? parts[1].trim() : "";

    if (firstToken.equalsIgnoreCase(Config.BROADCAST_ALL_TARGET)) {
      if (remainingText.isEmpty()) {
        consoleLogger.logInfo(Config.SERVER_BROADCAST_USAGE);
        return;
      }

      broadcastToAll(remainingText);
      return;
    }

    if (hasSecondPart) {
      if (remainingText.isEmpty()) {
        consoleLogger.logInfo(Config.SERVER_BROADCAST_USAGE);
        return;
      }

      broadcastToClient(firstToken, remainingText);
      return;
    }

    broadcastToAll(payloadCandidate);
  }

  private void broadcastToAll(String message) {
    String payload = Config.SERVER_BROADCAST_PREFIX + message;
    clientBroadcaster.broadcast(payload);
    consoleLogger.logInfo(
        "Broadcast ALL enviado a " + clientBroadcaster.connectedClientsCount() + " cliente(s).");
  }

  private void broadcastToClient(String clientId, String message) {
    String payload = Config.SERVER_BROADCAST_PREFIX + message;
    boolean delivered = clientBroadcaster.broadcastToClient(clientId, payload);

    if (!delivered) {
      consoleLogger.logInfo("No existe cliente con ID: " + clientId);
      return;
    }

    consoleLogger.logInfo("Broadcast enviado a cliente '" + clientId + "'.");
  }
}
