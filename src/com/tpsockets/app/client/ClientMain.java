package com.tpsockets.app.client;

import com.tpsockets.infrastructure.network.ChatClient;
import com.tpsockets.shared.Config;

public class ClientMain {

  public static void main(String[] args) {
    ChatClient chatClient = new ChatClient(Config.HOST, Config.PORT);
    chatClient.start();
  }
}
