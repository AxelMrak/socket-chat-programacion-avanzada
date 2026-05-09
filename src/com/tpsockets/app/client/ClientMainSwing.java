package com.tpsockets.app.client;

import com.tpsockets.ui.client.ClientWindow;
import com.tpsockets.ui.shared.SwingTheme;
import javax.swing.SwingUtilities;

public class ClientMainSwing {

  public static void main(String[] args) {
    SwingTheme.applyLookAndFeel();

    SwingUtilities.invokeLater(() -> {
      ClientWindow clientWindow = new ClientWindow();
      SwingTheme.applyTheme(clientWindow.getFrame());
      clientWindow.getFrame().setVisible(true);
      clientWindow.showConnectionDialog();
    });
  }

  public static void launchClient(String clientId, String host, int port) {
    SwingUtilities.invokeLater(() -> {
      ClientWindow clientWindow = new ClientWindow();
      SwingTheme.applyTheme(clientWindow.getFrame());
      clientWindow.getFrame().setTitle("Socket Chat - " + clientId);
      clientWindow.getFrame().setVisible(true);
      clientWindow.autoConnect(clientId, host, port);
    });
  }
}
