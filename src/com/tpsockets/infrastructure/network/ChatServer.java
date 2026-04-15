package com.tpsockets.infrastructure.network;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.infrastructure.console.ConsoleLogger;
import java.util.Objects;

public class ChatServer {

  private final int port;
  private final MessageProcessor messageProcessor;
  private final ConsoleLogger consoleLogger;

  public ChatServer(int port, MessageProcessor messageProcessor, ConsoleLogger consoleLogger) {

    boolean isPortValid = port > 0 && port <= 65535;

    if (!isPortValid) {
      throw new IllegalArgumentException("Puerto inválido: " + port);
    }

    Objects.requireNonNull(messageProcessor, "MessageProcessor no puede ser null");
    Objects.requireNonNull(consoleLogger, "ConsoleLogger no puede ser null");

    this.port = port;
    this.messageProcessor = messageProcessor;
    this.consoleLogger = consoleLogger;
  }

  public void start() {
    try (var serverSocket = new java.net.ServerSocket(port)) {
      consoleLogger.logInfo("Servidor iniciado en el puerto " + port);

      while (true) {
        var clientSocket = serverSocket.accept();
        consoleLogger.logInfo("Cliente aceptado: " + clientSocket.getRemoteSocketAddress());

        var handler = new ClientSessionHandler(clientSocket, messageProcessor, consoleLogger);
        new Thread(handler).start();
      }
    } catch (java.io.IOException e) {
      consoleLogger.logError("Error en el servidor: " + e.getMessage());
    }
  }
}
