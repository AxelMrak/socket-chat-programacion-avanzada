package com.tpsockets.app.server;

import com.tpsockets.domain.DefaultMessageProcessor;
import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.domain.bet.BetCoordinator;
import com.tpsockets.domain.bet.BetMatchCatalog;
import com.tpsockets.domain.bet.BetLogRepository;
import com.tpsockets.infrastructure.bet.TextFileBetLogRepository;
import com.tpsockets.infrastructure.console.ConsoleLogger;
import com.tpsockets.infrastructure.network.ClientBroadcaster;
import com.tpsockets.infrastructure.network.ChatServer;
import com.tpsockets.infrastructure.network.InMemoryClientBroadcaster;
import com.tpsockets.shared.Config;
import com.tpsockets.shared.logging.AppLogger;
import java.nio.file.Path;

public class ServerMain {

  public static void main(String[] args) {
    AppLogger logger = new ConsoleLogger();
    MessageProcessor messageProcessor = new DefaultMessageProcessor();
    ClientBroadcaster clientBroadcaster = new InMemoryClientBroadcaster();
    BetLogRepository betLogRepository = new TextFileBetLogRepository(Path.of(Config.BET_LOG_FILE));
    BetCoordinator betCoordinator =
        new BetCoordinator(new BetMatchCatalog(), betLogRepository, clientBroadcaster, logger);
    ChatServer chatServer =
        new ChatServer(Config.PORT, messageProcessor, logger, clientBroadcaster, betCoordinator);

    chatServer.start();
  }
}
