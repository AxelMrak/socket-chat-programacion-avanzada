package com.tpsockets.domain;

import com.tpsockets.shared.Config;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DefaultMessageProcessor implements MessageProcessor {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @Override
  public String process(String message) {
    boolean isEmptyMessage = message == null || message.trim().isEmpty();

    if (isEmptyMessage) {
      return "Empty message received";
    }

    String trimmedMessage = message.trim();
    String normalizedMessage = trimmedMessage.toUpperCase();
    boolean isUpperCommand = normalizedMessage.equals("UPPER") || normalizedMessage.startsWith("UPPER ");

    switch (normalizedMessage) {
      case "HELLO":
        return welcomeMessage();
      case "TIME":
        return timeMessage();
      case "DATE":
      return dateMessage();
      case "HELP":
        return helpMessage();
      case "MUNDIAL":
        return worldCupFixtureMessage();
      case Config.EXIT_COMMAND:
        return exitMessage();
      default:
        if (isUpperCommand) {
          return upperMessage(trimmedMessage);
        }
        return defaultMessage();
    }
  }

  private String welcomeMessage() {
    return "Bienvenido al servidor de mensajes. Envíe 'TIME' para obtener la hora actual, 'DATE' para la fecha, 'UPPER <mensaje>' para convertir a mayúsculas, o 'EXIT' para salir.";
  }

  private String timeMessage() {
    return "TIEMPO ACTUAL: " + LocalTime.now().format(TIME_FORMATTER);
  }

  private String dateMessage() {
    return "FECHA ACTUAL: " + LocalDate.now().format(DATE_FORMATTER);
  }

  private String exitMessage() {
    return "Adios!";
  }

  private String upperMessage(String message) {

    String content = message.substring(5).trim();

    if (content.isEmpty()) {
      return "No se proporcionó ningún mensaje para convertir a mayúsculas. Use 'UPPER <mensaje>'.";
    }

    return content.toUpperCase();
  }

  private String helpMessage() {
    return "Comandos: HELLO, TIME, DATE, UPPER <mensaje>, MUNDIAL, SALIR.";
  }

  private String defaultMessage() {
    return "Comando no reconocido. Envíe 'HELP' para obtener ayuda.";
  }

  private String worldCupFixtureMessage() {
    return "Mundial 2026: inicia el 11/06 con México vs Sudáfrica en Ciudad de México; fase de grupos del 11/06 al 27/06; dieciseisavos del 28/06 al 03/07; octavos del 04/07 al 07/07; cuartos el 09, 10 y 11/07; semifinales el 14 y 15/07; tercer puesto el 18/07 en Miami; final el 19/07 en Nueva York/Nueva Jersey.";
  }
}
