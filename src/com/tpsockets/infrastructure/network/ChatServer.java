package com.tpsockets.infrastructure.network;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.domain.bet.BetCoordinator;
import com.tpsockets.shared.logging.AppLogger;
import java.util.Objects;

public class ChatServer {

  private final int port;
  private final MessageProcessor messageProcessor;
  private final AppLogger logger;
  private final ClientBroadcaster clientBroadcaster;
  private final BetCoordinator betCoordinator;
  private final ServerOperatorConsole serverOperatorConsole;

  public ChatServer(
      int port,
      MessageProcessor messageProcessor,
      AppLogger logger,
      ClientBroadcaster clientBroadcaster,
      BetCoordinator betCoordinator) {

    boolean isPortValid = port > 0 && port <= 65535;

    if (!isPortValid) {
      throw new IllegalArgumentException("Puerto inválido: " + port);
    }

    Objects.requireNonNull(messageProcessor, "MessageProcessor no puede ser null");
    Objects.requireNonNull(logger, "Logger no puede ser null");
    Objects.requireNonNull(clientBroadcaster, "ClientBroadcaster no puede ser null");
    Objects.requireNonNull(betCoordinator, "BetCoordinator no puede ser null");

    this.port = port;
    this.messageProcessor = messageProcessor;
    this.logger = logger;
    this.clientBroadcaster = clientBroadcaster;
    this.betCoordinator = betCoordinator;
    var broadcastCommandProcessor = new BroadcastCommandProcessor(clientBroadcaster, logger);
    this.serverOperatorConsole = new ServerOperatorConsole(broadcastCommandProcessor, logger);
  }

  public void start() {
    serverOperatorConsole.start();

    try (var serverSocket = new java.net.ServerSocket(port)) {
      logger.logInfo("Servidor iniciado en el puerto " + port);

      while (true) {
        var clientSocket = serverSocket.accept();
        logger.logInfo("Cliente aceptado: " + clientSocket.getRemoteSocketAddress());

        var handler =
            new ClientSessionHandler(clientSocket, messageProcessor, logger, clientBroadcaster, betCoordinator);
        new Thread(handler).start();
      }
    } catch (java.io.IOException e) {
      logger.logError("Error en el servidor: " + e.getMessage());
    }
  }
}
