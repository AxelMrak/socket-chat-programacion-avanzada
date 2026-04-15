package com.tpsockets.shared;

public class Config {
  public static final int PORT = 3000;
  public static final String HOST = "localhost";
  public static final String EXIT_COMMAND = "SALIR";
  public static final String WELCOME_MSG = "--- Proyecto AXEL - TP Sockets ---\nIngrese un mensaje para enviar al servidor (o '"
      + EXIT_COMMAND + "' para salir):";
  public static final String SERVER_STARTED_MSG = "Servidor iniciado en el puerto " + PORT + ". Esperando conexiones...";
  public static final String CLIENT_CONNECTED_MSG = "Cliente conectado.";
  public static final String CLIENT_DISCONNECTED_MSG = "Cliente desconectado.";
}
