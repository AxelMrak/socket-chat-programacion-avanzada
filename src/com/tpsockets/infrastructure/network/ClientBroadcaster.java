package com.tpsockets.infrastructure.network;

import java.io.PrintWriter;

public interface ClientBroadcaster {

  boolean register(String clientId, PrintWriter writer);

  void unregister(String clientId);

  void broadcast(String message);

  boolean broadcastToClient(String clientId, String message);

  int connectedClientsCount();

  default void println(PrintWriter writer, String message) {
    synchronized (writer) {
      writer.println(message);
    }
  }
}
