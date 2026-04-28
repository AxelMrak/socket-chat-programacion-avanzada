package com.tpsockets.infrastructure.console;

import com.tpsockets.shared.logging.AppLogger;

public class ConsoleLogger implements AppLogger {

  @Override
  public void logInfo(String message) {
    System.out.println("[INFO] " + message);
  }

  @Override
  public void logReceived(String message) {
    System.out.println("[RECEIVED] " + message);
  }

  @Override
  public void logSent(String message) {
    System.out.println("[SENT] " + message);
  }

  @Override
  public void logConnection(String clientInfo) {
    System.out.println("[CONNECTION] Cliente conectado: " + clientInfo);
  }

  @Override
  public void logDisconnection(String clientInfo) {
    System.out.println("[DISCONNECTION] Cliente desconectado: " + clientInfo);
  }

  @Override
  public void logError(String errorMessage) {
    System.err.println("[ERROR] " + errorMessage);
  }
}
