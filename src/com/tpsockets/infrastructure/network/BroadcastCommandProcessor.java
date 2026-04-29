package com.tpsockets.infrastructure.network;

import com.tpsockets.shared.Config;
import com.tpsockets.shared.logging.AppLogger;
import java.util.Locale;
import java.util.Objects;

public class BroadcastCommandProcessor {

  private final ClientBroadcaster clientBroadcaster;
  private final AppLogger logger;

  public BroadcastCommandProcessor(ClientBroadcaster clientBroadcaster, AppLogger logger) {
    this.clientBroadcaster = Objects.requireNonNull(clientBroadcaster, "ClientBroadcaster no puede ser null");
    this.logger = Objects.requireNonNull(logger, "Logger no puede ser null");
  }

  public boolean process(String rawLine) {
    if (rawLine == null) {
      return false;
    }

    String trimmedLine = rawLine.trim();

    if (trimmedLine.isEmpty()) {
      return false;
    }

    String normalizedLine = trimmedLine.toUpperCase(Locale.ROOT);
    boolean isBroadcastCommand = normalizedLine.equals(Config.SERVER_BROADCAST_COMMAND);
    boolean isBroadcastWithMessage = normalizedLine.startsWith(Config.SERVER_BROADCAST_COMMAND + " ");

    if (!isBroadcastCommand && !isBroadcastWithMessage) {
      return false;
    }

    processBroadcastCommand(trimmedLine);
    return true;
  }

  private void processBroadcastCommand(String trimmedLine) {
    String payloadCandidate = trimmedLine.substring(Config.SERVER_BROADCAST_COMMAND.length()).trim();

    if (payloadCandidate.isEmpty()) {
      logger.logInfo(Config.SERVER_BROADCAST_USAGE);
      return;
    }

    String[] parts = payloadCandidate.split("\\s+", 2);
    String firstToken = parts[0];
    boolean hasSecondPart = parts.length > 1;
    String remainingText = hasSecondPart ? parts[1].trim() : "";

    if (firstToken.equalsIgnoreCase(Config.BROADCAST_ALL_TARGET)) {
      if (remainingText.isEmpty()) {
        logger.logInfo(Config.SERVER_BROADCAST_USAGE);
        return;
      }

      broadcastToAll(remainingText);
      return;
    }

    if (hasSecondPart) {
      if (remainingText.isEmpty()) {
        logger.logInfo(Config.SERVER_BROADCAST_USAGE);
        return;
      }

      broadcastToClient(firstToken, remainingText);
      return;
    }

    broadcastToAll(payloadCandidate);
  }

  private void broadcastToAll(String message) {
    String payload = Config.SERVER_BROADCAST_PREFIX + message;
    clientBroadcaster.broadcast(payload);
    logger.logInfo("Broadcast ALL enviado a " + clientBroadcaster.connectedClientsCount() + " cliente(s).");
  }

  private void broadcastToClient(String clientId, String message) {
    String payload = Config.SERVER_BROADCAST_PREFIX + message;
    boolean delivered = clientBroadcaster.broadcastToClient(clientId, payload);

    if (!delivered) {
      logger.logInfo("No existe cliente con ID: " + clientId);
      return;
    }

    logger.logInfo("Broadcast enviado a cliente '" + clientId + "'.");
  }
}
