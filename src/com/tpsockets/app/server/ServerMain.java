package com.tpsockets.app.server;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.infrastructure.console.ConsoleLogger;
import com.tpsockets.infrastructure.network.ClientBroadcaster;
import com.tpsockets.infrastructure.network.ChatServer;
import com.tpsockets.infrastructure.network.InMemoryClientBroadcaster;
import com.tpsockets.shared.Config;

public class ServerMain {

  public static void main(String[] args) {
    ConsoleLogger logger = new ConsoleLogger();
    MessageProcessor messageProcessor = new com.tpsockets.domain.DefaultMessageProcessor();
    ClientBroadcaster clientBroadcaster = new InMemoryClientBroadcaster();
    ChatServer chatServer = new ChatServer(Config.PORT, messageProcessor, logger, clientBroadcaster);

    chatServer.start();
  }
}
