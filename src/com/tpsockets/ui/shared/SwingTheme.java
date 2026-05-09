package com.tpsockets.ui.shared;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

public final class SwingTheme {

  public static final Color COLOR_BACKGROUND = Color.BLACK;
  public static final Color COLOR_PANEL = new Color(0x111111);
  public static final Color COLOR_TEXT = new Color(0xe0e0e0);
  public static final Color COLOR_TEXT_SUBTLE = new Color(0x999999);
  public static final Color COLOR_ACCENT = new Color(0x4a9eff);
  public static final Color COLOR_SUCCESS = new Color(0x4caf50);
  public static final Color COLOR_WARNING = new Color(0xffc107);
  public static final Color COLOR_ERROR = new Color(0xf44336);
  public static final Color COLOR_BORDER = new Color(0x333333);
  public static final Color COLOR_SURFACE = new Color(0x1a1a1a);
  public static final Color COLOR_OVERLAY = new Color(0x222222);

  public static final String FONT_FAMILY = "SansSerif";
  public static final int FONT_SIZE_REGULAR = 14;
  public static final int FONT_SIZE_HEADER = 16;

  public static final Font FONT_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_REGULAR);
  public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_HEADER);
  public static final Font FONT_HEADER = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_HEADER);

  public static final Border BORDER_PANEL = BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(COLOR_BORDER, 1),
      BorderFactory.createEmptyBorder(12, 12, 12, 12)
  );

  public static final Border BORDER_INPUT = BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(COLOR_BORDER, 1),
      BorderFactory.createEmptyBorder(6, 8, 6, 8)
  );

  public static final Border BORDER_SECTION = BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
      BorderFactory.createEmptyBorder(8, 12, 8, 12)
  );

  public static void applyLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      try {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
          | UnsupportedLookAndFeelException ex) {
        System.err.println("No se pudo configurar LookAndFeel: " + ex.getMessage());
      }
    }

    UIManager.put("Panel.background", COLOR_PANEL);
    UIManager.put("Label.foreground", COLOR_TEXT);
    UIManager.put("Label.font", FONT_REGULAR);
    UIManager.put("Button.background", COLOR_SURFACE);
    UIManager.put("Button.foreground", COLOR_TEXT);
    UIManager.put("Button.font", FONT_REGULAR);
    UIManager.put("TextField.background", COLOR_SURFACE);
    UIManager.put("TextField.foreground", COLOR_TEXT);
    UIManager.put("TextField.caretForeground", COLOR_ACCENT);
    UIManager.put("TextArea.background", COLOR_SURFACE);
    UIManager.put("TextArea.foreground", COLOR_TEXT);
    UIManager.put("ScrollPane.background", COLOR_PANEL);
    UIManager.put("Viewport.background", COLOR_PANEL);
  }

  public static void applyTheme(JFrame frame) {
    Objects.requireNonNull(frame, "El frame no puede ser null");
    applyLookAndFeel();
    frame.getContentPane().setBackground(COLOR_BACKGROUND);
    frame.setForeground(COLOR_TEXT);
    applyDarkRecursively(frame.getContentPane());
  }

  public static void applyDarkRecursively(java.awt.Container container) {
    for (java.awt.Component comp : container.getComponents()) {
      if (comp instanceof javax.swing.JPanel) {
        comp.setBackground(COLOR_PANEL);
        ((javax.swing.JComponent) comp).setOpaque(true);
      } else if (comp instanceof javax.swing.JScrollPane) {
        comp.setBackground(COLOR_PANEL);
        ((javax.swing.JScrollPane) comp).getViewport().setBackground(COLOR_PANEL);
        ((javax.swing.JScrollPane) comp).getViewport().setOpaque(true);
      } else if (comp instanceof javax.swing.JSplitPane) {
        comp.setBackground(COLOR_PANEL);
        ((javax.swing.JComponent) comp).setOpaque(false);
      } else if (comp instanceof javax.swing.JList) {
        comp.setBackground(COLOR_SURFACE);
      } else if (comp instanceof javax.swing.JTable) {
        comp.setBackground(COLOR_SURFACE);
        javax.swing.JTable table = (javax.swing.JTable) comp;
        table.getTableHeader().setBackground(COLOR_PANEL);
      } else if (comp instanceof javax.swing.JTextPane) {
        comp.setBackground(COLOR_SURFACE);
      } else if (comp instanceof javax.swing.JTextArea) {
        comp.setBackground(COLOR_SURFACE);
      } else if (comp instanceof javax.swing.JTextField) {
        comp.setBackground(COLOR_SURFACE);
      } else if (comp instanceof javax.swing.JButton) {
        comp.setBackground(COLOR_SURFACE);
      }
      if (comp instanceof java.awt.Container && !(comp instanceof javax.swing.JViewport)) {
        applyDarkRecursively((java.awt.Container) comp);
      }
    }
  }

  public static void configurePanel(JPanel panel) {
    Objects.requireNonNull(panel, "El panel no puede ser null");
    panel.setBackground(COLOR_PANEL);
    panel.setForeground(COLOR_TEXT);
    panel.setFont(FONT_REGULAR);
    panel.setBorder(BORDER_PANEL);
    panel.setOpaque(true);
  }

  public static JLabel createLabel(String texto, Color color) {
    Objects.requireNonNull(texto, "El texto no puede ser null");
    JLabel label = new JLabel(texto);
    label.setFont(FONT_REGULAR);
    label.setForeground(color != null ? color : COLOR_TEXT);
    label.setOpaque(false);
    return label;
  }

  public static JLabel createHeaderLabel(String texto) {
    Objects.requireNonNull(texto, "El texto no puede ser null");
    JLabel label = new JLabel(texto);
    label.setFont(FONT_HEADER);
    label.setForeground(COLOR_ACCENT);
    label.setOpaque(false);
    return label;
  }

  public static void styleButton(JButton button) {
    Objects.requireNonNull(button, "El boton no puede ser null");
    button.setBackground(COLOR_SURFACE);
    button.setForeground(COLOR_TEXT);
    button.setFont(FONT_REGULAR);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setOpaque(true);
    button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

    Color hoverBackground = COLOR_ACCENT;
    Color hoverForeground = COLOR_BACKGROUND;

    button.addMouseListener(new MouseAdapter() {
      private Color originalBackground;
      private Color originalForeground;

      @Override
      public void mouseEntered(MouseEvent e) {
        originalBackground = button.getBackground();
        originalForeground = button.getForeground();
        button.setBackground(hoverBackground);
        button.setForeground(hoverForeground);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        button.setBackground(originalBackground);
        button.setForeground(originalForeground);
      }
    });
  }

  public static void styleTextArea(JTextArea area) {
    Objects.requireNonNull(area, "El area de texto no puede ser null");
    area.setBackground(COLOR_PANEL);
    area.setForeground(COLOR_TEXT);
    area.setFont(FONT_REGULAR);
    area.setCaretColor(COLOR_ACCENT);
    area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    area.setOpaque(true);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
  }

  public static void styleScrollPane(JScrollPane pane) {
    Objects.requireNonNull(pane, "El scroll pane no puede ser null");
    pane.setBackground(COLOR_PANEL);
    pane.getViewport().setBackground(COLOR_PANEL);
    pane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
    pane.setOpaque(true);

    for (Component comp : pane.getComponents()) {
      if (comp instanceof JViewport) {
        comp.setBackground(COLOR_PANEL);
      }
    }
  }

  public static void styleTextField(JTextField field) {
    Objects.requireNonNull(field, "El campo de texto no puede ser null");
    field.setBackground(COLOR_SURFACE);
    field.setForeground(COLOR_TEXT);
    field.setFont(FONT_REGULAR);
    field.setCaretColor(COLOR_ACCENT);
    field.setBorder(BORDER_INPUT);
    field.setOpaque(true);
  }

  public static Border createThemeBorder() {
    return BorderFactory.createLineBorder(COLOR_BORDER, 1);
  }

  public static Border createEmptyPadding() {
    return BorderFactory.createEmptyBorder(12, 12, 12, 12);
  }

  public static JPanel createPanel() {
    JPanel panel = new JPanel();
    panel.setBackground(COLOR_PANEL);
    panel.setForeground(COLOR_TEXT);
    panel.setFont(FONT_REGULAR);
    panel.setOpaque(true);
    return panel;
  }

  public static JScrollPane createScrollPane(Component view) {
    JScrollPane pane = new JScrollPane(view);
    pane.setBackground(COLOR_PANEL);
    pane.getViewport().setBackground(COLOR_PANEL);
    pane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
    pane.setOpaque(false);
    pane.getViewport().setOpaque(false);
    return pane;
  }

  private SwingTheme() {
    throw new UnsupportedOperationException("SwingTheme es una clase utilitaria y no puede ser instanciada");
  }
}
