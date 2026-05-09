package com.tpsockets.app.dashboard;

import com.tpsockets.app.client.ClientMainSwing;
import com.tpsockets.app.server.ServerMainSwing;
import com.tpsockets.shared.Config;
import com.tpsockets.ui.shared.SwingTheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Dashboard {

  private final JFrame frame;
  private final JLabel serverStatusLabel;
  private final JTextArea logArea;
  private final JButton startServerButton;
  private final JButton stopServerButton;
  private final JTextField portField;
  private final JTextField clientNameField;

  private ServerMainSwing serverInstance;
  private volatile boolean serverRunning;

  public Dashboard() {
    frame = new JFrame("Socket Chat - Dashboard");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 500);
    frame.setLocationRelativeTo(null);

    serverStatusLabel = new JLabel("Servidor: Detenido");
    serverStatusLabel.setFont(SwingTheme.FONT_BOLD);
    serverStatusLabel.setForeground(SwingTheme.COLOR_ERROR);

    startServerButton = new JButton("Iniciar servidor");
    SwingTheme.styleButton(startServerButton);
    startServerButton.setBackground(SwingTheme.COLOR_SUCCESS);
    startServerButton.setForeground(SwingTheme.COLOR_BACKGROUND);

    stopServerButton = new JButton("Detener servidor");
    SwingTheme.styleButton(stopServerButton);
    stopServerButton.setBackground(SwingTheme.COLOR_ERROR);
    stopServerButton.setForeground(SwingTheme.COLOR_BACKGROUND);
    stopServerButton.setEnabled(false);

    portField = new JTextField(String.valueOf(Config.PORT), 6);
    portField.setFont(SwingTheme.FONT_REGULAR);
    portField.setBackground(SwingTheme.COLOR_SURFACE);
    portField.setForeground(SwingTheme.COLOR_TEXT);
    portField.setCaretColor(SwingTheme.COLOR_TEXT);
    portField.setBorder(SwingTheme.BORDER_INPUT);
    portField.setHorizontalAlignment(JTextField.CENTER);

    clientNameField = new JTextField("user_" + System.currentTimeMillis() % 10000, 15);
    clientNameField.setFont(SwingTheme.FONT_REGULAR);
    clientNameField.setBackground(SwingTheme.COLOR_SURFACE);
    clientNameField.setForeground(SwingTheme.COLOR_TEXT);
    clientNameField.setCaretColor(SwingTheme.COLOR_TEXT);
    clientNameField.setBorder(SwingTheme.BORDER_INPUT);

    logArea = new JTextArea();
    logArea.setEditable(false);
    logArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
    logArea.setBackground(SwingTheme.COLOR_SURFACE);
    logArea.setForeground(SwingTheme.COLOR_TEXT);
    logArea.setLineWrap(true);
    logArea.setWrapStyleWord(true);

    buildLayout();
    attachListeners();
  }

  private void buildLayout() {
    frame.setLayout(new BorderLayout(8, 8));

    JPanel headerPanel = SwingTheme.createPanel();
    headerPanel.setLayout(new BorderLayout());
    headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

    JLabel title = new JLabel("Socket Chat - Panel");
    title.setFont(SwingTheme.FONT_HEADER);
    title.setForeground(SwingTheme.COLOR_ACCENT);
    headerPanel.add(title, BorderLayout.NORTH);

    headerPanel.add(serverStatusLabel, BorderLayout.SOUTH);

    frame.add(headerPanel, BorderLayout.NORTH);

    JPanel controlPanel = SwingTheme.createPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
    controlPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(SwingTheme.COLOR_BORDER),
        "Controles del servidor",
        javax.swing.border.TitledBorder.LEFT,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        SwingTheme.FONT_BOLD,
        SwingTheme.COLOR_ACCENT));

    JPanel portRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    portRow.setOpaque(false);
    portRow.add(new JLabel("Puerto:"));
    portRow.add(portField);
    controlPanel.add(portRow);

    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    buttonRow.setOpaque(false);
    buttonRow.add(startServerButton);
    buttonRow.add(stopServerButton);
    controlPanel.add(buttonRow);

    frame.add(controlPanel, BorderLayout.CENTER);

    JPanel clientPanel = SwingTheme.createPanel();
    clientPanel.setLayout(new BoxLayout(clientPanel, BoxLayout.Y_AXIS));
    clientPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(SwingTheme.COLOR_BORDER),
        "Clientes",
        javax.swing.border.TitledBorder.LEFT,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        SwingTheme.FONT_BOLD,
        SwingTheme.COLOR_ACCENT));

    JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    nameRow.setOpaque(false);
    nameRow.add(new JLabel("Nombre:"));
    nameRow.add(clientNameField);
    clientPanel.add(nameRow);

    JButton addClientButton = new JButton("Agregar cliente");
    SwingTheme.styleButton(addClientButton);
    addClientButton.setBackground(SwingTheme.COLOR_ACCENT);
    addClientButton.setForeground(SwingTheme.COLOR_BACKGROUND);
    addClientButton.addActionListener(e -> addClientWindow());

    JPanel clientButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    clientButtonRow.setOpaque(false);
    clientButtonRow.add(addClientButton);
    clientPanel.add(clientButtonRow);

    frame.add(clientPanel, BorderLayout.EAST);

    JPanel logPanel = SwingTheme.createPanel();
    logPanel.setLayout(new BorderLayout());
    logPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(SwingTheme.COLOR_BORDER),
        "Registro de actividad",
        javax.swing.border.TitledBorder.LEFT,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        SwingTheme.FONT_BOLD,
        SwingTheme.COLOR_ACCENT));

    JScrollPane logScroll = SwingTheme.createScrollPane(logArea);
    logPanel.add(logScroll, BorderLayout.CENTER);

    frame.add(logPanel, BorderLayout.SOUTH);
  }

  private void attachListeners() {
    startServerButton.addActionListener(e -> startServer());
    stopServerButton.addActionListener(e -> stopServer());
  }

  private void startServer() {
    int port;
    try {
      port = Integer.parseInt(portField.getText().trim());
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(frame, "Puerto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    log("Iniciando servidor en puerto " + port + "...");
    startServerButton.setEnabled(false);
    portField.setEnabled(false);

    serverInstance = new ServerMainSwing();
    serverInstance.start(port, this::onServerLog, this::onServerStarted, this::onServerStopped);
  }

  private void stopServer() {
    if (serverInstance != null) {
      log("Deteniendo servidor...");
      serverInstance.stop();
    }
  }

  private void onServerLog(String message) {
    SwingUtilities.invokeLater(() -> log(message));
  }

  private void onServerStarted() {
    SwingUtilities.invokeLater(() -> {
      serverRunning = true;
      serverStatusLabel.setText("Servidor: Activo");
      serverStatusLabel.setForeground(SwingTheme.COLOR_SUCCESS);
      startServerButton.setEnabled(false);
      stopServerButton.setEnabled(true);
      portField.setEnabled(false);
    });
  }

  private void onServerStopped() {
    SwingUtilities.invokeLater(() -> {
      serverRunning = false;
      serverStatusLabel.setText("Servidor: Detenido");
      serverStatusLabel.setForeground(SwingTheme.COLOR_ERROR);
      startServerButton.setEnabled(true);
      stopServerButton.setEnabled(false);
      portField.setEnabled(true);
    });
  }

  private void addClientWindow() {
    if (!serverRunning) {
      JOptionPane.showMessageDialog(frame, "Primero iniciá el servidor.", "Servidor no iniciado", JOptionPane.WARNING_MESSAGE);
      return;
    }

    String clientName = clientNameField.getText().trim();
    if (clientName.isEmpty()) {
      JOptionPane.showMessageDialog(frame, "Ingresá un nombre de cliente.", "Nombre inválido", JOptionPane.WARNING_MESSAGE);
      return;
    }

    log("Creando cliente: " + clientName);
    ClientMainSwing.launchClient(clientName, Config.HOST, Config.PORT);

    clientNameField.setText("user_" + System.currentTimeMillis() % 10000);
  }

  private void log(String message) {
    String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    logArea.append("[" + timestamp + "] " + message + "\n");
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }

  public void show() {
    frame.setVisible(true);
  }

  public JFrame getFrame() {
    return frame;
  }
}
