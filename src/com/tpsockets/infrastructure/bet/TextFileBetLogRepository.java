package com.tpsockets.infrastructure.bet;

import com.tpsockets.domain.bet.BetEntry;
import com.tpsockets.domain.bet.BetLogRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TextFileBetLogRepository implements BetLogRepository {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final Path path;

  public TextFileBetLogRepository(Path path) {
    this.path = Objects.requireNonNull(path, "path no puede ser null");
  }

  @Override
  public synchronized void append(BetEntry betEntry) {
    Objects.requireNonNull(betEntry, "betEntry no puede ser null");

    String line =
        DATE_TIME_FORMATTER.format(betEntry.timestamp())
            + " | client="
            + betEntry.clientId()
            + " | matchId="
            + betEntry.matchId()
            + " | match='"
            + betEntry.matchTitle()
            + "' | team="
            + betEntry.team()
            + " | amount="
            + betEntry.amount()
            + System.lineSeparator();

    try {
      Files.writeString(path, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo persistir apuesta en archivo: " + path, e);
    }
  }
}
