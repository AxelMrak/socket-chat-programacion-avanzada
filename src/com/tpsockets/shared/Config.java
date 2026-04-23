package com.tpsockets.shared;

public class Config {
  public static final int PORT = 3000;
  public static final String HOST = "localhost";
  public static final String EXIT_COMMAND = "SALIR";
  public static final String SERVER_BROADCAST_COMMAND = "BROADCAST";
  public static final String BROADCAST_ALL_TARGET = "ALL";
  public static final String SERVER_BROADCAST_PREFIX = "[SERVER] ";
  public static final String SERVER_BROADCAST_USAGE = "Uso: BROADCAST <mensaje> | BROADCAST ALL <mensaje> | BROADCAST <CLIENT_ID> <mensaje>";
  public static final String CLIENT_ID_PROMPT = "Ingrese su nombre de cliente (3-20, letras/números/_/-):";
  public static final String CLIENT_ID_ASSIGNED_PREFIX = "CLIENT_ID_OK:";
  public static final String CLIENT_ID_ASSIGNED_MESSAGE = CLIENT_ID_ASSIGNED_PREFIX;
  public static final String CLIENT_ID_INVALID_MESSAGE = "Nombre inválido. Use 3 a 20 caracteres: letras, números, '_' o '-'.";
  public static final String CLIENT_ID_IN_USE_MESSAGE = "Ese nombre ya está en uso. Elegí otro.";
  public static final String WELCOME_MSG = "\n--- Proyecto AXEL - TP Sockets ---\nIngrese un mensaje para enviar al servidor (o '"
      + EXIT_COMMAND + "' para salir):";
  public static final String SERVER_STARTED_MSG = "Servidor iniciado en el puerto " + PORT
      + ". Esperando conexiones...";
  public static final String CLIENT_CONNECTED_MSG = "Cliente conectado.";
  public static final String CLIENT_DISCONNECTED_MSG = "Cliente desconectado.";
}
