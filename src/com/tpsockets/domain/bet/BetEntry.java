package com.tpsockets.domain.bet;

import java.time.LocalDateTime;
import java.util.Objects;

public record BetEntry(String clientId, int matchId, String matchTitle, String team, double amount, LocalDateTime timestamp) {

  public BetEntry {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    Objects.requireNonNull(matchTitle, "matchTitle no puede ser null");
    Objects.requireNonNull(team, "team no puede ser null");
    Objects.requireNonNull(timestamp, "timestamp no puede ser null");
    if (clientId.isBlank() || matchTitle.isBlank() || team.isBlank()) {
      throw new IllegalArgumentException("Campos de texto no pueden ser vacíos");
    }
    if (matchId <= 0) {
      throw new IllegalArgumentException("matchId inválido");
    }
    if (amount <= 0) {
      throw new IllegalArgumentException("amount debe ser mayor a cero");
    }
  }
}
