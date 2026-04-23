package com.tpsockets.infrastructure.network;

import java.io.PrintWriter;

public interface ClientBroadcaster {

  boolean register(String clientId, PrintWriter writer);

  void unregister(String clientId);

  void broadcast(String message);

  boolean broadcastToClient(String clientId, String message);

  boolean hasClient(String clientId);

  int connectedClientsCount();
}
