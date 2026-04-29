package com.tpsockets.domain;

import com.tpsockets.shared.Config;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DefaultMessageProcessor implements MessageProcessor {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private final CommandDispatcher commandDispatcher;

  public DefaultMessageProcessor() {
    this(createDefaultHandlers());
  }

  public DefaultMessageProcessor(Map<String, ChatCommandHandler> commandHandlers) {
    this.commandDispatcher = new CommandDispatcher(commandHandlers);
  }

  @Override
  public String process(String message) {
    if (message == null || message.trim().isEmpty()) {
      return "Empty message received";
    }

    ParsedCommand parsedCommand = ParsedCommand.from(message);
    boolean hasArguments = !parsedCommand.arguments().isBlank();
    boolean isUpperCommand = parsedCommand.command().equalsIgnoreCase("UPPER");

    if (hasArguments && !isUpperCommand) {
      return defaultMessage();
    }

    String response = commandDispatcher.dispatch(parsedCommand.command(), parsedCommand.arguments());

    if (response != null) {
      return response;
    }

    return defaultMessage();
  }

  private static Map<String, ChatCommandHandler> createDefaultHandlers() {
    return Map.of(
        "HELLO", ignoredArguments -> welcomeMessage(),
        "TIME", ignoredArguments -> timeMessage(),
        "DATE", ignoredArguments -> dateMessage(),
        "HELP", ignoredArguments -> helpMessage(),
        "MUNDIAL", ignoredArguments -> worldCupFixtureMessage(),
        Config.EXIT_COMMAND, ignoredArguments -> exitMessage(),
        "UPPER", DefaultMessageProcessor::upperMessage);
  }

  private static String welcomeMessage() {
    return "Bienvenido al servidor de mensajes. Envíe 'TIME' para obtener la hora actual, 'DATE' para la fecha, 'UPPER <mensaje>' para convertir a mayúsculas, o '"
        + Config.EXIT_COMMAND
        + "' para salir.";
  }

  private static String timeMessage() {
    return "TIEMPO ACTUAL: " + LocalTime.now().format(TIME_FORMATTER);
  }

  private static String dateMessage() {
    return "FECHA ACTUAL: " + LocalDate.now().format(DATE_FORMATTER);
  }

  private static String exitMessage() {
    return "Adios!";
  }

  private static String upperMessage(String arguments) {
    if (arguments == null || arguments.trim().isEmpty()) {
      return "No se proporcionó ningún mensaje para convertir a mayúsculas. Use 'UPPER <mensaje>'.";
    }

    return arguments.trim().toUpperCase();
  }

  private static String helpMessage() {
    return "Comandos: HELLO, TIME, DATE, UPPER <mensaje>, MUNDIAL, " + Config.EXIT_COMMAND + ".";
  }

  private static String defaultMessage() {
    return "Comando no reconocido. Envíe 'HELP' para obtener ayuda.";
  }

  private static String worldCupFixtureMessage() {
    return "Mundial 2026: inicia el 11/06 con México vs Sudáfrica en Ciudad de México; fase de grupos del 11/06 al 27/06; dieciseisavos del 28/06 al 03/07; octavos del 04/07 al 07/07; cuartos el 09, 10 y 11/07; semifinales el 14 y 15/07; tercer puesto el 18/07 en Miami; final el 19/07 en Nueva York/Nueva Jersey.";
  }

  private record ParsedCommand(String command, String arguments) {

    private static ParsedCommand from(String rawMessage) {
      String trimmedMessage = rawMessage.trim();
      String[] tokens = trimmedMessage.split("\\s+", 2);
      String command = tokens[0];
      String arguments = tokens.length > 1 ? tokens[1] : "";
      return new ParsedCommand(command, arguments);
    }
  }
}
