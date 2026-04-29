package com.tpsockets.domain;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CommandDispatcher {

  private final Map<String, ChatCommandHandler> commandHandlers;

  public CommandDispatcher(Map<String, ChatCommandHandler> commandHandlers) {
    Objects.requireNonNull(commandHandlers, "commandHandlers no puede ser null");
    this.commandHandlers = new HashMap<>();

    for (Map.Entry<String, ChatCommandHandler> entry : commandHandlers.entrySet()) {
      String commandName = normalizeCommand(entry.getKey());
      ChatCommandHandler commandHandler = Objects.requireNonNull(entry.getValue(), "commandHandler no puede ser null");
      this.commandHandlers.put(commandName, commandHandler);
    }
  }

  public String dispatch(String commandName, String arguments) {
    ChatCommandHandler commandHandler = commandHandlers.get(normalizeCommand(commandName));

    if (commandHandler == null) {
      return null;
    }

    return commandHandler.handle(arguments);
  }

  private String normalizeCommand(String commandName) {
    if (commandName == null) {
      throw new IllegalArgumentException("commandName no puede ser null");
    }

    String trimmedCommand = commandName.trim();

    if (trimmedCommand.isEmpty()) {
      throw new IllegalArgumentException("commandName no puede ser vacío");
    }

    return trimmedCommand.toUpperCase(Locale.ROOT);
  }
}
