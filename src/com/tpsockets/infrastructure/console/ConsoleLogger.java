package com.tpsockets.infrastructure.console;

public class ConsoleLogger {

  public void logInfo(String message) {
    System.out.println("[INFO] " + message);
  }

  public void logReceived(String message) {
    System.out.println("[RECEIVED] " + message);
  }

  public void logSent(String message) {
    System.out.println("[SENT] " + message);
  }

  public void logConnection(String clientInfo) {
    System.out.println("[CONNECTION] Cliente conectado: " + clientInfo);
  }

  public void logDisconnection(String clientInfo) {
    System.out.println("[DISCONNECTION] Cliente desconectado: " + clientInfo);
  }

  public void logError(String errorMessage) {
    System.err.println("[ERROR] " + errorMessage);
  }
}
