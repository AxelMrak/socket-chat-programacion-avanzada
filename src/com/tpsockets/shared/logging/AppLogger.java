package com.tpsockets.shared.logging;

public interface AppLogger {

  void logInfo(String message);

  void logReceived(String message);

  void logSent(String message);

  void logConnection(String clientInfo);

  void logDisconnection(String clientInfo);

  void logError(String errorMessage);
}
