package com.tpsockets.ui.client;

import com.tpsockets.shared.Config;
import com.tpsockets.ui.shared.MessageFormatter;
import com.tpsockets.ui.shared.SwingTheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Container;

public class ClientWindow {

  private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

  private final JFrame frame;
  private final JTextArea chatArea;
  private final JTextField inputField;
  private final JButton sendButton;
  private final JButton betButton;

  private Socket socket;
  private BufferedReader reader;
  private PrintWriter writer;
  private String clientId;
  private volatile boolean connected;
  private volatile BetState betState = BetState.IDLE;
  private String pendingMatchList;

  public ClientWindow() {
    frame = new JFrame("Socket Chat - Client");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(700, 550);
    frame.setLocationRelativeTo(null);

    chatArea = new JTextArea();
    chatArea.setEditable(false);
    chatArea.setFont(SwingTheme.FONT_REGULAR);
    chatArea.setBackground(SwingTheme.COLOR_SURFACE);
    chatArea.setForeground(SwingTheme.COLOR_TEXT);
    chatArea.setLineWrap(true);
    chatArea.setWrapStyleWord(true);

    inputField = new JTextField();
    inputField.setFont(SwingTheme.FONT_REGULAR);
    inputField.setBackground(SwingTheme.COLOR_SURFACE);
    inputField.setForeground(SwingTheme.COLOR_TEXT);
    inputField.setCaretColor(SwingTheme.COLOR_TEXT);
    inputField.setBorder(SwingTheme.BORDER_INPUT);
    inputField.setEnabled(false);

    sendButton = new JButton("Enviar");
    SwingTheme.styleButton(sendButton);
    sendButton.setEnabled(false);

    betButton = new JButton("Apostar");
    SwingTheme.styleButton(betButton);
    betButton.setEnabled(false);
    betButton.addActionListener(e -> startBetFlow());

    buildLayout();
    attachListeners();
  }

  private void buildLayout() {
    frame.setLayout(new BorderLayout(6, 6));

    JPanel topBar = new JPanel(new BorderLayout());
    topBar.setBackground(SwingTheme.COLOR_PANEL);
    topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

    JLabel statusLabel = new JLabel("Desconectado");
    statusLabel.setFont(SwingTheme.FONT_BOLD);
    statusLabel.setForeground(SwingTheme.COLOR_ERROR);
    statusLabel.setName("statusLabel");
    topBar.add(statusLabel, BorderLayout.WEST);

    frame.add(topBar, BorderLayout.NORTH);

    JScrollPane chatScroll = SwingTheme.createScrollPane(chatArea);
    frame.add(chatScroll, BorderLayout.CENTER);

    JPanel bottomPanel = SwingTheme.createPanel();
    bottomPanel.setLayout(new BorderLayout(6, 0));
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    JPanel inputRow = new JPanel(new BorderLayout(6, 0));
    inputRow.setOpaque(false);
    inputRow.add(inputField, BorderLayout.CENTER);
    inputRow.add(sendButton, BorderLayout.EAST);

    bottomPanel.add(inputRow, BorderLayout.CENTER);
    bottomPanel.add(betButton, BorderLayout.EAST);

    frame.add(bottomPanel, BorderLayout.SOUTH);
  }

  private void attachListeners() {
    sendButton.addActionListener(e -> sendMessage());
    inputField.addActionListener(e -> sendMessage());
  }

  private void sendMessage() {
    String message = inputField.getText().trim();
    if (message.isEmpty() || !connected) {
      return;
    }
    writer.println(message);
    inputField.setText("");

    if (message.equalsIgnoreCase(Config.EXIT_COMMAND)) {
      disconnect();
    }
  }

  public void showConnectionDialog() {
    JPanel panel = SwingTheme.createPanel();
    panel.setLayout(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JPanel formPanel = new JPanel(new BorderLayout(8, 8));
    formPanel.setOpaque(false);

    JTextField hostField = new JTextField(Config.HOST);
    hostField.setFont(SwingTheme.FONT_REGULAR);
    hostField.setBackground(SwingTheme.COLOR_SURFACE);
    hostField.setForeground(SwingTheme.COLOR_TEXT);
    hostField.setCaretColor(SwingTheme.COLOR_TEXT);
    hostField.setBorder(SwingTheme.BORDER_INPUT);

    JTextField portField = new JTextField(String.valueOf(Config.PORT));
    portField.setFont(SwingTheme.FONT_REGULAR);
    portField.setBackground(SwingTheme.COLOR_SURFACE);
    portField.setForeground(SwingTheme.COLOR_TEXT);
    portField.setCaretColor(SwingTheme.COLOR_TEXT);
    portField.setBorder(SwingTheme.BORDER_INPUT);

    formPanel.add(new JLabel("Servidor:"), BorderLayout.WEST);
    formPanel.add(hostField, BorderLayout.CENTER);

    JPanel portRow = new JPanel(new BorderLayout(8, 8));
    portRow.setOpaque(false);
    portRow.add(new JLabel("Puerto:"), BorderLayout.WEST);
    portRow.add(portField, BorderLayout.CENTER);

    panel.add(formPanel, BorderLayout.CENTER);
    panel.add(portRow, BorderLayout.SOUTH);

    int result = JOptionPane.showConfirmDialog(
        frame, panel, "Conectar al servidor",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result != JOptionPane.OK_OPTION) {
      System.exit(0);
      return;
    }

    String host = hostField.getText().trim();
    String portText = portField.getText().trim();

    if (host.isEmpty() || portText.isEmpty()) {
      JOptionPane.showMessageDialog(frame, "Completá el host y el puerto.", "Error", JOptionPane.ERROR_MESSAGE);
      showConnectionDialog();
      return;
    }

    int port;
    try {
      port = Integer.parseInt(portText);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(frame, "Puerto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
      showConnectionDialog();
      return;
    }

    connect(host, port);
  }

  private void connect(String host, int port) {
    appendToChat("Conectando a " + host + ":" + port + "...\n");

    new Thread(() -> {
      try {
        socket = new Socket(host, port);
        reader = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        appendToChat("Connected. Performing handshake...\n");

        if (!performHandshake()) {
          appendToChat("Handshake failed.\n");
          return;
        }

        connected = true;
        SwingUtilities.invokeLater(() -> {
          inputField.setEnabled(true);
          sendButton.setEnabled(true);
          betButton.setEnabled(true);
          JLabel status = (JLabel) ((Container) frame.getContentPane().getComponent(0)).getComponent(0);
          status.setText("Connected as: " + clientId);
          status.setForeground(SwingTheme.COLOR_SUCCESS);
        });

        appendToChat(Config.WELCOME_MSG + "\n");

        startServerReader();

      } catch (ConnectException e) {
        appendToChat("Connection refused: server not available at " + host + ":" + port + "\n");
      } catch (IOException e) {
        appendToChat("Connection error: " + e.getMessage() + "\n");
      }
    }, "ConectorCliente").start();
  }

  private boolean performHandshake() throws IOException {
    while (true) {
      String serverPrompt = reader.readLine();
      if (serverPrompt == null) {
        return false;
      }

      String requestedId = showHandshakeDialog(serverPrompt);
      if (requestedId == null) {
        return false;
      }

      writer.println(requestedId);

      String serverReply = reader.readLine();
      if (serverReply == null) {
        return false;
      }

      appendToChat(serverReply + "\n");

      if (serverReply.startsWith(Config.CLIENT_ID_ASSIGNED_PREFIX)) {
        clientId = serverReply.substring(Config.CLIENT_ID_ASSIGNED_PREFIX.length());
        return true;
      }
    }
  }

  private String showHandshakeDialog(String prompt) {
    JTextField idField = new JTextField();
    idField.setFont(SwingTheme.FONT_REGULAR);
    idField.setBackground(SwingTheme.COLOR_SURFACE);
    idField.setForeground(SwingTheme.COLOR_TEXT);
    idField.setCaretColor(SwingTheme.COLOR_TEXT);
    idField.setBorder(SwingTheme.BORDER_INPUT);

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBackground(SwingTheme.COLOR_PANEL);
    panel.add(new JLabel(prompt), BorderLayout.NORTH);
    panel.add(idField, BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(
        frame, panel, "Identificación",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result != JOptionPane.OK_OPTION) {
      return null;
    }

    return idField.getText().trim();
  }

  private void startServerReader() {
    Thread readerThread = new Thread(() -> {
      try {
        String serverMessage;
        while ((serverMessage = reader.readLine()) != null) {
          String finalMessage = serverMessage;
          SwingUtilities.invokeLater(() -> handleServerMessage(finalMessage));
        }
      } catch (IOException e) {
        appendToChat("\nConnection to server lost.\n");
      } finally {
        disconnect();
      }
    }, "LectorServidor");
    readerThread.setDaemon(true);
    readerThread.start();
  }

  private void handleServerMessage(String message) {
    if (message.contains(Config.BET_PROMPT_SELECT_MATCH)) {
      betState = BetState.SHOWING_MATCH_LIST;
      showMatchSelectionDialog();
    } else if (message.contains(Config.BET_PROMPT_PLACE_BET)) {
      betState = BetState.WAITING_BET_INPUT;
      showBetInputdDialog();
    } else if (message.startsWith("Partidos disponibles")) {
      pendingMatchList = message;
      betState = BetState.WAITING_MATCH_LIST;
      appendToChat(message + "\n");
    } else {
      appendToChat(message + "\n");
    }
  }

  private void startBetFlow() {
    if (!connected) {
      return;
    }

    writer.println(Config.BET_COMMAND);
  }

  private void appendToChat(String message) {
    SwingUtilities.invokeLater(() -> {
      chatArea.append(message);
      chatArea.setCaretPosition(chatArea.getDocument().getLength());
    });
  }

  private void disconnect() {
    connected = false;
    SwingUtilities.invokeLater(() -> {
      inputField.setEnabled(false);
      sendButton.setEnabled(false);
      betButton.setEnabled(false);
      JLabel status = (JLabel) ((Container) frame.getContentPane().getComponent(0)).getComponent(0);
      status.setText("Desconectado");
      status.setForeground(SwingTheme.COLOR_ERROR);
    });

    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
    }
  }

  public JFrame getFrame() {
    return frame;
  }

  public boolean isConnected() {
    return connected;
  }

  public String getClientId() {
    return clientId;
  }

  public void autoConnect(String clientId, String host, int port) {
    connect(host, port);

    javax.swing.Timer handshakeTimer = new javax.swing.Timer(500, e -> {
      if (writer != null && clientId != null) {
        writer.println(clientId);
        ((javax.swing.Timer) e.getSource()).stop();
      }
    });
    handshakeTimer.setRepeats(false);
    handshakeTimer.start();
  }

  private void showMatchSelectionDialog() {
    String matchList = pendingMatchList != null ? pendingMatchList : "No matches available.";
    pendingMatchList = null;

    JTextArea matchArea = new JTextArea(matchList);
    matchArea.setEditable(false);
    matchArea.setFont(SwingTheme.FONT_REGULAR);
    matchArea.setBackground(SwingTheme.COLOR_SURFACE);
    matchArea.setForeground(SwingTheme.COLOR_TEXT);
    matchArea.setCaretColor(SwingTheme.COLOR_TEXT);

    JTextField matchIdField = new JTextField();
    matchIdField.setFont(SwingTheme.FONT_REGULAR);
    matchIdField.setBackground(SwingTheme.COLOR_SURFACE);
    matchIdField.setForeground(SwingTheme.COLOR_TEXT);
    matchIdField.setCaretColor(SwingTheme.COLOR_TEXT);
    matchIdField.setBorder(SwingTheme.BORDER_INPUT);

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBackground(SwingTheme.COLOR_PANEL);
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    panel.add(new JScrollPane(matchArea), BorderLayout.CENTER);

    JPanel inputRow = new JPanel(new BorderLayout(8, 0));
    inputRow.setOpaque(false);
    inputRow.add(new JLabel("Partido #:"), BorderLayout.WEST);
    inputRow.add(matchIdField, BorderLayout.CENTER);
    panel.add(inputRow, BorderLayout.SOUTH);

    int result = JOptionPane.showConfirmDialog(
        frame, panel, "Elegir partido",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String input = matchIdField.getText().trim();
      if (input.equalsIgnoreCase(Config.BET_CANCEL_COMMAND)) {
        writer.println(Config.BET_CANCEL_COMMAND);
        betState = BetState.IDLE;
      } else if (!input.isEmpty()) {
        writer.println(input);
      }
    } else {
      writer.println(Config.BET_CANCEL_COMMAND);
      betState = BetState.IDLE;
    }
  }

  private void showBetInputdDialog() {
    JTextField betField = new JTextField();
    betField.setFont(SwingTheme.FONT_REGULAR);
    betField.setBackground(SwingTheme.COLOR_SURFACE);
    betField.setForeground(SwingTheme.COLOR_TEXT);
    betField.setCaretColor(SwingTheme.COLOR_TEXT);
    betField.setBorder(SwingTheme.BORDER_INPUT);

    JLabel hintLabel = new JLabel("Formato: <equipo> <monto>");
    hintLabel.setForeground(SwingTheme.COLOR_TEXT_SUBTLE);
    hintLabel.setFont(SwingTheme.FONT_REGULAR);

    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBackground(SwingTheme.COLOR_PANEL);
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    panel.add(hintLabel, BorderLayout.NORTH);
    panel.add(betField, BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(
        frame, panel, "Apostar",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String input = betField.getText().trim();
      if (input.equalsIgnoreCase(Config.BET_CANCEL_COMMAND)) {
        writer.println(Config.BET_CANCEL_COMMAND);
      } else if (!input.isEmpty()) {
        writer.println(input);
      }
    } else {
      writer.println(Config.BET_CANCEL_COMMAND);
    }
    betState = BetState.IDLE;
  }

  private enum BetState {
    IDLE,
    WAITING_MATCH_LIST,
    SHOWING_MATCH_LIST,
    WAITING_BET_INPUT
  }
}
