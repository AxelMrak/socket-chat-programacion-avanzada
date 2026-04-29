package com.tpsockets.domain.bet;

import com.tpsockets.infrastructure.network.ClientBroadcaster;
import com.tpsockets.shared.logging.AppLogger;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

public class BetCoordinator {

  private final BetMatchCatalog betMatchCatalog;
  private final BetLogRepository betLogRepository;
  private final ClientBroadcaster clientBroadcaster;
  private final AppLogger logger;

  public BetCoordinator(
      BetMatchCatalog betMatchCatalog,
      BetLogRepository betLogRepository,
      ClientBroadcaster clientBroadcaster,
      AppLogger logger) {
    this.betMatchCatalog = Objects.requireNonNull(betMatchCatalog, "betMatchCatalog no puede ser null");
    this.betLogRepository = Objects.requireNonNull(betLogRepository, "betLogRepository no puede ser null");
    this.clientBroadcaster = Objects.requireNonNull(clientBroadcaster, "clientBroadcaster no puede ser null");
    this.logger = Objects.requireNonNull(logger, "logger no puede ser null");
  }

  public String renderMatchList() {
    StringBuilder builder = new StringBuilder("Partidos disponibles para apostar:\n");
    for (BetMatch match : betMatchCatalog.listAll()) {
      builder.append(match.id()).append(") ").append(match.title()).append("\n");
    }
    return builder.toString().trim();
  }

  public boolean isValidMatchId(int matchId) {
    return betMatchCatalog.findById(matchId).isPresent();
  }

  public String placeBet(int matchId, String clientId, String rawInput) {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    Objects.requireNonNull(rawInput, "rawInput no puede ser null");

    BetMatch match =
        betMatchCatalog
            .findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Partido inválido: " + matchId));

    String[] parts = rawInput.trim().split("\\s+", 2);
    if (parts.length < 2) {
      return "Formato inválido. Use '<equipo> <monto>'.";
    }

    String team = parts[0].trim();
    String normalizedTeam = team.toLowerCase(Locale.ROOT);
    boolean validTeam =
        match.teams().stream().map(t -> t.toLowerCase(Locale.ROOT)).anyMatch(t -> t.equals(normalizedTeam));
    if (!validTeam) {
      return "Equipo inválido para ese partido. Opciones: " + String.join(", ", match.teams());
    }

    double amount;
    try {
      amount = Double.parseDouble(parts[1].trim());
    } catch (NumberFormatException e) {
      return "Monto inválido. Debe ser numérico y mayor a cero.";
    }

    if (amount <= 0) {
      return "Monto inválido. Debe ser mayor a cero.";
    }

    BetEntry betEntry = new BetEntry(clientId, match.id(), match.title(), team, amount, LocalDateTime.now());
    betLogRepository.append(betEntry);

    String realtimeMessage = "Cliente " + clientId + " apostó " + amount + " a " + team + " en " + match.title();
    clientBroadcaster.broadcast(realtimeMessage);
    logger.logInfo("Apuesta registrada: " + realtimeMessage);
    return "Apuesta registrada correctamente.";
  }
}
