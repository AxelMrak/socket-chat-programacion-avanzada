package com.tpsockets.domain.bet;

import java.util.List;
import java.util.Objects;

public record BetMatch(int id, String title, List<String> teams) {

  public BetMatch {
    if (id <= 0) {
      throw new IllegalArgumentException("id debe ser mayor a cero");
    }
    Objects.requireNonNull(title, "title no puede ser null");
    Objects.requireNonNull(teams, "teams no puede ser null");
    if (title.isBlank()) {
      throw new IllegalArgumentException("title no puede ser vacío");
    }
    if (teams.size() < 2) {
      throw new IllegalArgumentException("teams debe tener al menos dos equipos");
    }
  }
}
