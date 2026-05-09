package com.tpsockets.ui.shared;

import com.tpsockets.shared.logging.AppLogger;
import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class SwingLogger implements AppLogger {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Color COLOR_INFO       = Color.decode("#89b4fa");
  private static final Color COLOR_RECEIVED   = Color.decode("#cdd6f4");
  private static final Color COLOR_SENT       = Color.decode("#a6e3a1");
  private static final Color COLOR_CONNECTION = Color.decode("#f9e2af");
  private static final Color COLOR_DISCONNECT = Color.decode("#fab387");
  private static final Color COLOR_ERROR      = Color.decode("#f38ba8");
  private static final Color COLOR_TIMESTAMP  = Color.decode("#6c7086");

  private final JTextPane textPane;
  private final StyledDocument document;

  public SwingLogger(JTextPane textPane) {
    this.textPane = Objects.requireNonNull(textPane, "textPane no puede ser null");
    this.document = textPane.getStyledDocument();
  }

  @Override
  public void logInfo(String message) {
    appendLog("INFO", message, COLOR_INFO);
  }

  @Override
  public void logReceived(String message) {
    appendLog("RECV", message, COLOR_RECEIVED);
  }

  @Override
  public void logSent(String message) {
    appendLog("SENT", message, COLOR_SENT);
  }

  @Override
  public void logConnection(String clientInfo) {
    appendLog("CONN", "Cliente conectado: " + clientInfo, COLOR_CONNECTION);
  }

  @Override
  public void logDisconnection(String clientInfo) {
    appendLog("DISC", "Cliente desconectado: " + clientInfo, COLOR_DISCONNECT);
  }

  @Override
  public void logError(String errorMessage) {
    appendLog("ERROR", errorMessage, COLOR_ERROR);
  }

  private void appendLog(String label, String message, Color color) {
    String timestamp = LocalTime.now().format(TIME_FORMAT);
    String line = String.format("[%s] [%s] %s%n", timestamp, label, message);

    SwingUtilities.invokeLater(() -> {
      try {

        appendStyledText("[" + timestamp + "] ", COLOR_TIMESTAMP);

        appendStyledText("[" + label + "] ", color);

        appendStyledText(message + System.lineSeparator(), color);

        textPane.setCaretPosition(document.getLength());
      } catch (BadLocationException e) {

        System.err.println("[SwingLogger] Error al escribir en el documento: " + e.getMessage());
      }
    });
  }

  private void appendStyledText(String text, Color color) throws BadLocationException {
    Style style = document.addStyle("color-style", null);
    StyleConstants.setForeground(style, color);
    document.insertString(document.getLength(), text, style);
  }
}
