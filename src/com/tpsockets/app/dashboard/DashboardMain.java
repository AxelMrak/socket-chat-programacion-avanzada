package com.tpsockets.app.dashboard;

import com.tpsockets.ui.shared.SwingTheme;
import javax.swing.SwingUtilities;

public class DashboardMain {

  public static void main(String[] args) {
    SwingTheme.applyLookAndFeel();

    SwingUtilities.invokeLater(() -> {
      Dashboard dashboard = new Dashboard();
      SwingTheme.applyTheme(dashboard.getFrame());
      dashboard.show();
    });
  }
}
