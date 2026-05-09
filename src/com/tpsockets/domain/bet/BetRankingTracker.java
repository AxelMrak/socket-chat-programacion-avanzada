package com.tpsockets.domain.bet;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BetRankingTracker {

  private final Map<String, BetRanking> rankings = new ConcurrentHashMap<>();
  private final List<Runnable> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

  public void onChange(Runnable listener) {
    listeners.add(listener);
  }

  private void fireChanged() {
    for (Runnable listener : listeners) {
      listener.run();
    }
  }

  public BetRanking getOrCreateRanking(String clientId) {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    return rankings.computeIfAbsent(clientId, BetRanking::newRanking);
  }

  public List<BetRanking> getAllRankings() {
    return rankings.values().stream()
        .sorted(Comparator.comparingDouble(BetRanking::getRankingScore).reversed())
        .collect(Collectors.toList());
  }

  public Optional<BetRanking> getRanking(String clientId) {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    return Optional.ofNullable(rankings.get(clientId));
  }

  public void recordWin(String clientId, double amountWon) {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    rankings.compute(clientId, (id, current) -> {
      BetRanking ranking = current != null ? current : BetRanking.newRanking(id);
      return ranking.winBet(amountWon);
    });
    fireChanged();
  }

  public void recordLoss(String clientId, double amountLost) {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    rankings.compute(clientId, (id, current) -> {
      BetRanking ranking = current != null ? current : BetRanking.newRanking(id);
      return ranking.loseBet(amountLost);
    });
    fireChanged();
  }

  public void recordBetPlaced(String clientId, double amountWagered) {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    rankings.compute(clientId, (id, current) -> {
      BetRanking ranking = current != null ? current : BetRanking.newRanking(id);
      return ranking.placeBet(amountWagered);
    });
    fireChanged();
  }
}
