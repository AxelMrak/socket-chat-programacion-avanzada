package com.tpsockets.infrastructure.network;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.infrastructure.console.ConsoleLogger;
import com.tpsockets.shared.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSessionHandler implements Runnable {

  private final Socket socket;
  private final MessageProcessor messageProcessor;
  private final ConsoleLogger consoleLogger;

  public ClientSessionHandler(Socket socket, MessageProcessor messageProcessor, ConsoleLogger consoleLogger) {
    this.socket = socket;
    this.messageProcessor = messageProcessor;
    this.consoleLogger = consoleLogger;
  }

  @Override
  public void run() {
    String clientInfo = socket.getRemoteSocketAddress().toString();
    consoleLogger.logConnection(clientInfo);

    try (socket;
        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        var writer = new PrintWriter(socket.getOutputStream(), true)) {
      String line;
      while ((line = reader.readLine()) != null) {
        consoleLogger.logReceived(line);

        String response = messageProcessor.process(line);
        consoleLogger.logSent(response);
        writer.println(response);

        if (line.trim().equalsIgnoreCase(Config.EXIT_COMMAND)) {
          consoleLogger.logDisconnection(clientInfo);
          break;
        }
      }
    } catch (IOException e) {
      consoleLogger.logError("Error en sesión con " + clientInfo + ": " + e.getMessage());
    }
  }
}
