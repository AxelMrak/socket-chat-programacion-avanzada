package com.tpsockets.infrastructure.network;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryClientBroadcaster implements ClientBroadcaster {

  // Avoid RACE CONDITIONS using ConcurrentHashMap and synchronizing on PrintWriter when writing to it
  private final Map<String, PrintWriter> clientWritersById = new ConcurrentHashMap<>();

  @Override
  public boolean register(String clientId, PrintWriter writer) {
    String normalizedClientId = normalize(clientId);
    return clientWritersById.putIfAbsent(normalizedClientId, writer) == null;
  }

  @Override
  public void unregister(String clientId) {
    clientWritersById.remove(normalize(clientId));
  }

  @Override
  public void broadcast(String message) {
    for (PrintWriter writer : clientWritersById.values()) {
      synchronized (writer) {
        writer.println(message);
      }
    }
  }

  @Override
  public boolean broadcastToClient(String clientId, String message) {
    PrintWriter writer = clientWritersById.get(normalize(clientId));

    if (writer == null) {
      return false;
    }

    synchronized (writer) {
      writer.println(message);
    }

    return true;
  }

  @Override
  public boolean hasClient(String clientId) {
    return clientWritersById.containsKey(normalize(clientId));
  }

  @Override
  public int connectedClientsCount() {
    return clientWritersById.size();
  }

  private String normalize(String clientId) {
    return clientId.trim().toLowerCase(Locale.ROOT);
  }
}
