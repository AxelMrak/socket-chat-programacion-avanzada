package com.tpsockets.infrastructure.network;

import com.tpsockets.shared.logging.AppLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class ServerOperatorConsole {

  private final BroadcastCommandProcessor broadcastCommandProcessor;
  private final AppLogger logger;

  public ServerOperatorConsole(BroadcastCommandProcessor broadcastCommandProcessor, AppLogger logger) {
    this.broadcastCommandProcessor =
        Objects.requireNonNull(broadcastCommandProcessor, "BroadcastCommandProcessor no puede ser null");
    this.logger = Objects.requireNonNull(logger, "Logger no puede ser null");
  }

  public void start() {
    Thread operatorThread =
        new Thread(
            () -> {
              try {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                String line;

                while ((line = consoleReader.readLine()) != null) {
                  broadcastCommandProcessor.process(line);
                }
              } catch (IOException e) {
                logger.logError("Error leyendo consola de servidor: " + e.getMessage());
              }
            });

    operatorThread.setDaemon(true);
    operatorThread.start();
  }
}
