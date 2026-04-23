package com.tpsockets.infrastructure.network;

import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.infrastructure.console.ConsoleLogger;
import com.tpsockets.shared.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Pattern;

public class ClientSessionHandler implements Runnable {

  private final Socket socket;
  private final MessageProcessor messageProcessor;
  private final ConsoleLogger consoleLogger;
  private final ClientBroadcaster clientBroadcaster;
  private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

  public ClientSessionHandler(
      Socket socket,
      MessageProcessor messageProcessor,
      ConsoleLogger consoleLogger,
      ClientBroadcaster clientBroadcaster) {
    this.socket = socket;
    this.messageProcessor = messageProcessor;
    this.consoleLogger = consoleLogger;
    this.clientBroadcaster = clientBroadcaster;
  }

  @Override
  public void run() {
    String remoteAddress = socket.getRemoteSocketAddress().toString();

    String clientId = null;
    PrintWriter writer = null;

    try (socket; var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      writer = new PrintWriter(socket.getOutputStream(), true);
      clientId = resolveAndRegisterClientId(reader, writer);

      if (clientId == null) {
        return;
      }

      String clientInfo = clientId + " (" + remoteAddress + ")";
      consoleLogger.logConnection(clientInfo);

      String line;
      while ((line = reader.readLine()) != null) {
        consoleLogger.logReceived(line);

        String response = messageProcessor.process(line);
        consoleLogger.logSent(response);
        synchronized (writer) {
          writer.println(response);
        }

        if (line.trim().equalsIgnoreCase(Config.EXIT_COMMAND)) {
          break;
        }
      }
    } catch (IOException e) {
      consoleLogger.logError("Error en sesión con " + remoteAddress + ": " + e.getMessage());
    } finally {
      if (clientId != null) {
        clientBroadcaster.unregister(clientId);
        consoleLogger.logDisconnection(clientId + " (" + remoteAddress + ")");
      } else {
        consoleLogger.logDisconnection(remoteAddress);
      }
    }
  }

  private String resolveAndRegisterClientId(BufferedReader reader, PrintWriter writer) throws IOException {
    while (true) {
      synchronized (writer) {
        writer.println(Config.CLIENT_ID_PROMPT);
      }

      String candidate = reader.readLine();

      if (candidate == null) {
        return null;
      }

      String trimmedCandidate = candidate.trim();
      boolean isClientIdValid = CLIENT_ID_PATTERN.matcher(trimmedCandidate).matches();

      if (!isClientIdValid) {
        synchronized (writer) {
          writer.println(Config.CLIENT_ID_INVALID_MESSAGE);
        }
        continue;
      }

      boolean registered = clientBroadcaster.register(trimmedCandidate, writer);

      if (!registered) {
        synchronized (writer) {
          writer.println(Config.CLIENT_ID_IN_USE_MESSAGE);
        }
        continue;
      }

      synchronized (writer) {
        writer.println(Config.CLIENT_ID_ASSIGNED_MESSAGE + trimmedCandidate);
      }

      return trimmedCandidate;
    }
  }
}
