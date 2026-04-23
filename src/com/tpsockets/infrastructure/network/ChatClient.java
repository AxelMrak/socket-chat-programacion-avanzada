package com.tpsockets.infrastructure.network;

import com.tpsockets.shared.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

  private final String host;
  private final int port;

  public ChatClient(String host, int port) {
    boolean isHostValid = host != null && !host.isBlank();
    boolean isPortValid = port > 0 && port <= 65535;
    if (!isHostValid) {
      throw new IllegalArgumentException("Host no puede ser null o vacío");
    }
    if (!isPortValid) {
      throw new IllegalArgumentException("Puerto inválido: " + port);
    }
    this.host = host;
    this.port = port;
  }

  public void start() {
    try (var socket = new Socket(host, port);
        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        var writer = new PrintWriter(socket.getOutputStream(), true);
        var scanner = new Scanner(System.in)) {

      System.out.println("Conectado al servidor en " + host + ":" + port);

      performClientIdHandshake(reader, writer, scanner);
      System.out.println(Config.WELCOME_MSG);

      Thread serverReaderThread =
          new Thread(
              () -> {
                try {
                  String serverMessage;
                  while ((serverMessage = reader.readLine()) != null) {
                    System.out.println("\n" + serverMessage);
                    System.out.print("> ");
                  }
                } catch (IOException e) {
                  System.out.println("\nConexión con servidor cerrada.");
                }
              });

      serverReaderThread.setDaemon(true);
      serverReaderThread.start();

      while (true) {
        System.out.print("> ");
        String input = scanner.nextLine();

        writer.println(input);

        if (input.trim().equalsIgnoreCase(Config.EXIT_COMMAND)) {
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("Error de conexión: " + e.getMessage());
    }
  }

  private void performClientIdHandshake(
      BufferedReader reader, PrintWriter writer, Scanner scanner) throws IOException {
    while (true) {
      String serverPrompt = reader.readLine();

      if (serverPrompt == null) {
        throw new IOException("No se recibió prompt de identificación desde el servidor.");
      }

      System.out.println(serverPrompt);
      System.out.print("> ");
      String requestedClientId = scanner.nextLine();
      writer.println(requestedClientId);

      String serverReply = reader.readLine();

      if (serverReply == null) {
        throw new IOException("Conexión cerrada durante registro de nombre de cliente.");
      }

      System.out.println(serverReply);

      if (serverReply.startsWith(Config.CLIENT_ID_ASSIGNED_PREFIX)) {
        return;
      }
    }
  }
}
