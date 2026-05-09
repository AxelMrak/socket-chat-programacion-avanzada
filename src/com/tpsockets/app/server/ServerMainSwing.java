package com.tpsockets.app.server;

import com.tpsockets.domain.DefaultMessageProcessor;
import com.tpsockets.domain.MessageProcessor;
import com.tpsockets.domain.bet.BetCoordinator;
import com.tpsockets.domain.bet.BetMatchCatalog;
import com.tpsockets.domain.bet.BetLogRepository;
import com.tpsockets.domain.bet.BetRankingTracker;
import com.tpsockets.infrastructure.bet.TextFileBetLogRepository;
import com.tpsockets.infrastructure.network.BroadcastCommandProcessor;
import com.tpsockets.infrastructure.network.ClientBroadcaster;
import com.tpsockets.infrastructure.network.ClientSessionHandler;
import com.tpsockets.infrastructure.network.InMemoryClientBroadcaster;
import com.tpsockets.shared.Config;
import com.tpsockets.ui.server.ObservableBroadcaster;
import com.tpsockets.ui.server.ServerWindow;
import com.tpsockets.ui.shared.SwingTheme;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public class ServerMainSwing {

  private ServerSocket serverSocket;
  private Thread serverThread;
  private volatile boolean running;

  public static void main(String[] args) {
    SwingTheme.applyLookAndFeel();

    ServerMainSwing server = new ServerMainSwing();
    server.start(Config.PORT, null, () -> {}, () -> {});
  }

  public void start(int port, Consumer<String> logCallback, Runnable onStarted, Runnable onStopped) {
    ClientBroadcaster baseBroadcaster = new InMemoryClientBroadcaster();
    ObservableBroadcaster observableBroadcaster = new ObservableBroadcaster(baseBroadcaster);

    MessageProcessor messageProcessor = new DefaultMessageProcessor();
    BetLogRepository betLogRepository = new TextFileBetLogRepository(Path.of(Config.BET_LOG_FILE));

    SwingUtilities.invokeLater(() -> {
      BetRankingTracker rankingTracker = new BetRankingTracker();

      ServerWindow serverWindow = new ServerWindow(observableBroadcaster, rankingTracker);
      SwingTheme.applyTheme(serverWindow.getFrame());
      serverWindow.setVisible(true);

      BetCoordinator betCoordinator = new BetCoordinator(
          new BetMatchCatalog(), betLogRepository, observableBroadcaster, serverWindow.getLogger(), rankingTracker);

      BroadcastCommandProcessor commandProcessor = new BroadcastCommandProcessor(observableBroadcaster, serverWindow.getLogger());
      serverWindow.setCommandProcessor(commandProcessor);

      serverWindow.setStatusRunning();
      serverWindow.getLogger().logInfo("Iniciando servidor en puerto " + port + "...");

      if (logCallback != null) {
        logCallback.accept("Server starting on port " + port);
      }

      running = true;

      serverThread = new Thread(() -> {
        try (ServerSocket ss = new ServerSocket(port)) {
          serverSocket = ss;
          serverWindow.getLogger().logInfo("Servidor iniciado en puerto " + port);
          if (logCallback != null) {
            logCallback.accept("Server started on port " + port);
          }

          if (onStarted != null) {
            onStarted.run();
          }

          while (running) {
            try {
              Socket clientSocket = ss.accept();
              serverWindow.getLogger().logInfo("Cliente aceptado: " + clientSocket.getRemoteSocketAddress());
              if (logCallback != null) {
                logCallback.accept("Client accepted: " + clientSocket.getRemoteSocketAddress());
              }

              ClientSessionHandler handler = new ClientSessionHandler(
                  clientSocket, messageProcessor, serverWindow.getLogger(), observableBroadcaster, betCoordinator);
              new Thread(handler).start();
            } catch (IOException e) {
              if (running) {
                serverWindow.getLogger().logError("Error accepting client: " + e.getMessage());
              }
            }
          }
        } catch (IOException e) {
          serverWindow.getLogger().logError("Error en el servidor: " + e.getMessage());
          if (logCallback != null) {
            logCallback.accept("Server error: " + e.getMessage());
          }
        } finally {
          running = false;
          serverWindow.setStatusStopped();
          if (onStopped != null) {
            onStopped.run();
          }
        }
      }, "ServerThread");
      serverThread.setDaemon(true);
      serverThread.start();
    });
  }

  public void stop() {
    running = false;
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException e) {
    }
    if (serverThread != null) {
      serverThread.interrupt();
    }
  }

  public boolean isRunning() {
    return running;
  }
}
