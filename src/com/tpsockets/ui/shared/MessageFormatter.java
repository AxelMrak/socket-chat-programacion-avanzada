package com.tpsockets.ui.shared;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class MessageFormatter {

  private static final String SYSTEM_EVENT_PREFIX = "\u00BB"; 
  private static final String ERROR_PREFIX = "\u2717";       
  private static final String BET_RESULT_PREFIX = "\u2605";   
  private static final String TRUNCATION_SUFFIX = "...";

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

  private MessageFormatter() {
    throw new UnsupportedOperationException("Clase de utilidad: no se puede instanciar");
  }

  public static String formatTimestamp() {
    return "[" + LocalTime.now().format(TIME_FORMAT) + "]";
  }

  public static String formatServerMessage(String message) {
    return "[SERVER] " + message;
  }

  public static String formatClientMessage(String clientId, String message) {
    return clientId + ": " + message;
  }

  public static String formatSystemEvent(String event) {
    return SYSTEM_EVENT_PREFIX + " " + event;
  }

  public static String formatBroadcast(String from, String message) {
    return "[BROADCAST from " + from + "] " + message;
  }

  public static String formatBetResult(String message) {
    return BET_RESULT_PREFIX + " " + message;
  }

  public static String formatErrorMessage(String error) {
    return ERROR_PREFIX + " " + error;
  }

  public static String truncate(String text, int maxLength) {
    if (text == null) {
      return null;
    }
    if (maxLength <= 0) {
      return "";
    }
    if (text.length() <= maxLength) {
      return text;
    }
    if (maxLength <= TRUNCATION_SUFFIX.length()) {
      return TRUNCATION_SUFFIX;
    }
    return text.substring(0, maxLength - TRUNCATION_SUFFIX.length()) + TRUNCATION_SUFFIX;
  }
}
