package com.tpsockets.ui.server;

import com.tpsockets.domain.bet.BetRanking;
import com.tpsockets.domain.bet.BetRankingTracker;
import com.tpsockets.infrastructure.network.BroadcastCommandProcessor;
import com.tpsockets.ui.shared.SwingLogger;
import com.tpsockets.ui.shared.SwingTheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class ServerWindow {

  private final JFrame frame;
  private final ObservableBroadcaster broadcaster;
  private final BetRankingTracker rankingTracker;
  private final SwingLogger logger;

  private BroadcastCommandProcessor commandProcessor;

  private JList<String> clientList;
  private DefaultListModel<String> clientListModel;
  private JTable rankingTable;
  private RankingTableModel rankingTableModel;
  private JTextField operatorInput;
  private JLabel statusLabel;
  private JLabel clientCountLabel;

  public ServerWindow(
      ObservableBroadcaster broadcaster,
      BetRankingTracker rankingTracker) {
    this.broadcaster = broadcaster;
    this.rankingTracker = rankingTracker;

    frame = new JFrame("Socket Chat - Server");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1100, 700);
    frame.setLocationRelativeTo(null);

    JTextPane logPane = new JTextPane();
    logPane.setEditable(false);
    logger = new SwingLogger(logPane);

    buildLayout(logPane);
    attachBroadcasterListener();
    attachRankingListener();
  }

  private void buildLayout(JTextPane logPane) {
    frame.setLayout(new BorderLayout(8, 8));

    JPanel topBar = buildTopBar();
    frame.add(topBar, BorderLayout.NORTH);

    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mainSplit.setDividerLocation(280);
    mainSplit.setResizeWeight(0.25);
    mainSplit.setBackground(SwingTheme.COLOR_PANEL);
    mainSplit.setOpaque(false);

    JPanel leftPanel = buildLeftPanel();
    JScrollPane leftScroll = SwingTheme.createScrollPane(leftPanel);
    mainSplit.setLeftComponent(leftScroll);

    JPanel centerPanel = buildCenterPanel(logPane);
    mainSplit.setRightComponent(centerPanel);

    frame.add(mainSplit, BorderLayout.CENTER);

    JPanel bottomPanel = buildBottomPanel();
    frame.add(bottomPanel, BorderLayout.SOUTH);
  }

  private JPanel buildTopBar() {
    JPanel panel = SwingTheme.createPanel();
    panel.setLayout(new BorderLayout());

    statusLabel = new JLabel("  Servidor: Detenido", SwingConstants.LEFT);
    statusLabel.setFont(SwingTheme.FONT_BOLD);
    statusLabel.setForeground(SwingTheme.COLOR_WARNING);
    panel.add(statusLabel, BorderLayout.WEST);

    clientCountLabel = new JLabel("Clientes: 0  ", SwingConstants.RIGHT);
    clientCountLabel.setFont(SwingTheme.FONT_REGULAR);
    clientCountLabel.setForeground(SwingTheme.COLOR_TEXT_SUBTLE);
    panel.add(clientCountLabel, BorderLayout.EAST);

    return panel;
  }

  private JPanel buildLeftPanel() {
    JPanel panel = SwingTheme.createPanel();
    panel.setLayout(new BorderLayout(4, 4));

    JLabel clientsHeader = new JLabel("Clientes conectados");
    clientsHeader.setFont(SwingTheme.FONT_HEADER);
    clientsHeader.setForeground(SwingTheme.COLOR_ACCENT);
    clientsHeader.setBorder(SwingTheme.BORDER_SECTION);

    clientListModel = new DefaultListModel<>();
    clientList = new JList<>(clientListModel);
    clientList.setFont(SwingTheme.FONT_REGULAR);
    clientList.setForeground(SwingTheme.COLOR_TEXT);
    clientList.setBackground(SwingTheme.COLOR_SURFACE);
    clientList.setSelectionBackground(SwingTheme.COLOR_ACCENT);
    clientList.setSelectionForeground(SwingTheme.COLOR_BACKGROUND);
    JScrollPane listScroll = SwingTheme.createScrollPane(clientList);

    JPanel clientsSection = new JPanel(new BorderLayout());
    clientsSection.setOpaque(false);
    clientsSection.add(clientsHeader, BorderLayout.NORTH);
    clientsSection.add(listScroll, BorderLayout.CENTER);

    JLabel rankingHeader = new JLabel("Ranking de apuestas");
    rankingHeader.setFont(SwingTheme.FONT_HEADER);
    rankingHeader.setForeground(SwingTheme.COLOR_ACCENT);
    rankingHeader.setBorder(SwingTheme.BORDER_SECTION);

    rankingTableModel = new RankingTableModel();
    rankingTable = new JTable(rankingTableModel);
    rankingTable.setFont(SwingTheme.FONT_REGULAR);
    rankingTable.setForeground(SwingTheme.COLOR_TEXT);
    rankingTable.setBackground(SwingTheme.COLOR_SURFACE);
    rankingTable.setGridColor(SwingTheme.COLOR_BORDER);
    rankingTable.setSelectionBackground(SwingTheme.COLOR_ACCENT);
    rankingTable.setSelectionForeground(SwingTheme.COLOR_BACKGROUND);
    rankingTable.setShowGrid(true);
    rankingTable.setIntercellSpacing(new Dimension(1, 1));
    rankingTable.setRowHeight(22);
    rankingTable.setOpaque(true);
    rankingTable.getTableHeader().setFont(SwingTheme.FONT_BOLD);
    rankingTable.getTableHeader().setBackground(SwingTheme.COLOR_PANEL);
    rankingTable.getTableHeader().setForeground(SwingTheme.COLOR_ACCENT);
    rankingTable.getTableHeader().setOpaque(true);
    JScrollPane rankingScroll = SwingTheme.createScrollPane(rankingTable);

    JPanel rankingSection = new JPanel(new BorderLayout());
    rankingSection.setOpaque(false);
    rankingSection.add(rankingHeader, BorderLayout.NORTH);
    rankingSection.add(rankingScroll, BorderLayout.CENTER);

    JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, clientsSection, rankingSection);
    leftSplit.setDividerLocation(200);
    leftSplit.setResizeWeight(0.4);
    leftSplit.setDividerSize(4);
    leftSplit.setBorder(SwingTheme.BORDER_PANEL);
    leftSplit.setBackground(SwingTheme.COLOR_PANEL);
    leftSplit.setOpaque(false);

    panel.add(leftSplit, BorderLayout.CENTER);
    return panel;
  }

  private JPanel buildCenterPanel(JTextPane logPane) {
    JPanel panel = SwingTheme.createPanel();
    panel.setLayout(new BorderLayout(4, 4));

    JLabel logHeader = new JLabel("  Registro del servidor");
    logHeader.setFont(SwingTheme.FONT_HEADER);
    logHeader.setForeground(SwingTheme.COLOR_ACCENT);
    logHeader.setBorder(SwingTheme.BORDER_SECTION);
    panel.add(logHeader, BorderLayout.NORTH);

    JScrollPane logScroll = SwingTheme.createScrollPane(logPane);
    panel.add(logScroll, BorderLayout.CENTER);

    return panel;
  }

  private JPanel buildBottomPanel() {
    JPanel panel = SwingTheme.createPanel();
    panel.setLayout(new BorderLayout(6, 0));

    operatorInput = new JTextField();
    operatorInput.setFont(SwingTheme.FONT_REGULAR);
    operatorInput.setBackground(SwingTheme.COLOR_SURFACE);
    operatorInput.setForeground(SwingTheme.COLOR_TEXT);
    operatorInput.setCaretColor(SwingTheme.COLOR_TEXT);
    operatorInput.setBorder(SwingTheme.BORDER_INPUT);
    panel.add(operatorInput, BorderLayout.CENTER);

    JButton sendButton = new JButton("Enviar");
    SwingTheme.styleButton(sendButton);
    sendButton.addActionListener(e -> sendOperatorCommand());
    panel.add(sendButton, BorderLayout.EAST);

    operatorInput.addActionListener(e -> sendOperatorCommand());

    return panel;
  }

  private void sendOperatorCommand() {
    String command = operatorInput.getText().trim();
    if (command.isEmpty()) {
      return;
    }
    commandProcessor.process(command);
    operatorInput.setText("");
  }

  private void attachBroadcasterListener() {
    broadcaster.addListener(new ObservableBroadcaster.BroadcasterListener() {
      @Override
      public void onClientConnected(String clientId) {
        SwingUtilities.invokeLater(() -> {
          clientListModel.addElement(clientId);
          clientCountLabel.setText("Clientes: " + broadcaster.connectedClientsCount() + "  ");
        });
      }

      @Override
      public void onClientDisconnected(String clientId) {
        SwingUtilities.invokeLater(() -> {
          clientListModel.removeElement(clientId);
          clientCountLabel.setText("Clientes: " + broadcaster.connectedClientsCount() + "  ");
        });
      }

      @Override
      public void onClientListChanged(List<String> clientIds) {
        SwingUtilities.invokeLater(() -> {
          clientListModel.clear();
          for (String id : clientIds) {
            clientListModel.addElement(id);
          }
          clientCountLabel.setText("Clientes: " + clientIds.size() + "  ");
        });
      }
    });
  }

  private void attachRankingListener() {
    rankingTracker.onChange(() -> refreshRankings());
  }

  private void refreshRankings() {
    SwingUtilities.invokeLater(() -> {
      List<BetRanking> rankings = rankingTracker.getAllRankings();
      rankingTableModel.updateRankings(rankings);
    });
  }

  public void setStatusRunning() {
    SwingUtilities.invokeLater(() -> {
      statusLabel.setText("  Servidor: Activo");
      statusLabel.setForeground(SwingTheme.COLOR_SUCCESS);
    });
  }

  public void setStatusStopped() {
    SwingUtilities.invokeLater(() -> {
      statusLabel.setText("  Servidor: Detenido");
      statusLabel.setForeground(SwingTheme.COLOR_ERROR);
    });
  }

  public SwingLogger getLogger() {
    return logger;
  }

  public void setCommandProcessor(BroadcastCommandProcessor commandProcessor) {
    this.commandProcessor = commandProcessor;
  }

  public JFrame getFrame() {
    return frame;
  }

  public void setVisible(boolean visible) {
    frame.setVisible(visible);
  }

  private static class RankingTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"#", "Cliente", "Apuestas", "G", "P", "Win%", "Ganancia", "Puntaje"};

    private List<BetRanking> rankings = List.of();

    void updateRankings(List<BetRanking> rankings) {
      this.rankings = rankings != null ? rankings : List.of();
      fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
      return rankings.size();
    }

    @Override
    public int getColumnCount() {
      return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
      return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
      BetRanking r = rankings.get(row);
      return switch (column) {
        case 0 -> row + 1;
        case 1 -> r.clientId();
        case 2 -> r.totalBets();
        case 3 -> r.wonBets();
        case 4 -> r.lostBets();
        case 5 -> String.format("%.1f%%", r.getWinRate());
        case 6 -> String.format("$%.2f", r.getNetProfit());
        case 7 -> String.format("%.2f", r.getRankingScore());
        default -> "";
      };
    }
  }
}
