package com.tpsockets.app.server;

import com.tpsockets.domain.DefaultMessageProcessor;
import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.infrastructure.console.ConsoleLogger;
import com.tpsockets.infrastructure.network.ClientBroadcaster;
import com.tpsockets.infrastructure.network.ChatServer;
import com.tpsockets.infrastructure.network.InMemoryClientBroadcaster;
import com.tpsockets.shared.Config;
import com.tpsockets.shared.logging.AppLogger;

public class ServerMain {

  public static void main(String[] args) {
    AppLogger logger = new ConsoleLogger();
    MessageProcessor messageProcessor = new DefaultMessageProcessor();
    ClientBroadcaster clientBroadcaster = new InMemoryClientBroadcaster();
    ChatServer chatServer = new ChatServer(Config.PORT, messageProcessor, logger, clientBroadcaster);

    chatServer.start();
  }
}
