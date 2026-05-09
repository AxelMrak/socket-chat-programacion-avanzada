package com.tpsockets.ui.server;

import com.tpsockets.infrastructure.network.ClientBroadcaster;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public class ObservableBroadcaster implements ClientBroadcaster {

  @FunctionalInterface
  public interface BroadcasterListener {
    void onClientConnected(String clientId);

    default void onClientDisconnected(String clientId) {}

    default void onClientListChanged(List<String> clientIds) {}
  }

  private final ClientBroadcaster delegate;
  private final List<BroadcasterListener> listeners = new CopyOnWriteArrayList<>();
  private final Set<String> connectedClientIds = ConcurrentHashMap.newKeySet();

  public ObservableBroadcaster(ClientBroadcaster delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate no puede ser null");
  }

  public void addListener(BroadcasterListener listener) {
    Objects.requireNonNull(listener, "listener no puede ser null");
    listeners.add(listener);
  }

  public void removeListener(BroadcasterListener listener) {
    listeners.remove(listener);
  }

  public List<String> getConnectedClients() {
    return Collections.unmodifiableList(List.copyOf(connectedClientIds));
  }

  @Override
  public boolean register(String clientId, PrintWriter writer) {
    boolean registered = delegate.register(clientId, writer);

    if (registered) {
      String normalizedId = normalize(clientId);
      connectedClientIds.add(normalizedId);
      fireClientConnected(normalizedId);
      fireClientListChanged();
    }

    return registered;
  }

  @Override
  public void unregister(String clientId) {
    String normalizedId = normalize(clientId);
    delegate.unregister(clientId);
    connectedClientIds.remove(normalizedId);
    fireClientDisconnected(normalizedId);
    fireClientListChanged();
  }

  @Override
  public void broadcast(String message) {
    delegate.broadcast(message);
  }

  @Override
  public boolean broadcastToClient(String clientId, String message) {
    return delegate.broadcastToClient(clientId, message);
  }

  @Override
  public int connectedClientsCount() {
    return delegate.connectedClientsCount();
  }

  @Override
  public void println(PrintWriter writer, String message) {
    delegate.println(writer, message);
  }

  private void fireClientConnected(String clientId) {
    for (BroadcasterListener listener : listeners) {
      SwingUtilities.invokeLater(() -> listener.onClientConnected(clientId));
    }
  }

  private void fireClientDisconnected(String clientId) {
    for (BroadcasterListener listener : listeners) {
      SwingUtilities.invokeLater(() -> listener.onClientDisconnected(clientId));
    }
  }

  private void fireClientListChanged() {
    List<String> currentClients = getConnectedClients();
    for (BroadcasterListener listener : listeners) {
      SwingUtilities.invokeLater(() -> listener.onClientListChanged(currentClients));
    }
  }

  private String normalize(String clientId) {
    return clientId.trim().toLowerCase(Locale.ROOT);
  }
}
