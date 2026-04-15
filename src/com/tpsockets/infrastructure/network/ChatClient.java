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
      System.out.println(Config.WELCOME_MSG);

      while (true) {
        System.out.print("> ");
        String input = scanner.nextLine();

        writer.println(input);

        String response = reader.readLine();
        if (response == null) {
          System.out.println("El servidor cerró la conexión.");
          break;
        }

        System.out.println(response);

        if (input.trim().equalsIgnoreCase(Config.EXIT_COMMAND)) {
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("Error de conexión: " + e.getMessage());
    }
  }
}
