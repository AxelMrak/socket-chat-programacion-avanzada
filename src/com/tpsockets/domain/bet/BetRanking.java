package com.tpsockets.domain.bet;

import java.util.Objects;

public record BetRanking(String clientId, int totalBets, int wonBets, int lostBets,
                         double totalWagered, double totalWon) {

  public BetRanking {
    Objects.requireNonNull(clientId, "clientId no puede ser null");
    if (clientId.isBlank()) {
      throw new IllegalArgumentException("clientId no puede ser vacío");
    }
    if (totalBets < 0 || wonBets < 0 || lostBets < 0) {
      throw new IllegalArgumentException("Contadores de apuestas no pueden ser negativos");
    }
    if (totalWagered < 0.0 || totalWon < 0.0) {
      throw new IllegalArgumentException("Montos no pueden ser negativos");
    }
  }

  public static BetRanking newRanking(String clientId) {
    return new BetRanking(clientId, 0, 0, 0, 0.0, 0.0);
  }

  public BetRanking winBet(double amountWon) {
    if (amountWon <= 0.0) {
      throw new IllegalArgumentException("amountWon debe ser mayor a cero");
    }
    return new BetRanking(
        clientId,
        totalBets + 1,
        wonBets + 1,
        lostBets,
        totalWagered,
        totalWon + amountWon
    );
  }

  public BetRanking loseBet(double amountLost) {
    if (amountLost <= 0.0) {
      throw new IllegalArgumentException("amountLost debe ser mayor a cero");
    }
    return new BetRanking(
        clientId,
        totalBets + 1,
        wonBets,
        lostBets + 1,
        totalWagered + amountLost,
        totalWon
    );
  }

  public BetRanking placeBet(double amountWagered) {
    if (amountWagered <= 0.0) {
      throw new IllegalArgumentException("amountWagered debe ser mayor a cero");
    }
    return new BetRanking(
        clientId,
        totalBets + 1,
        wonBets,
        lostBets,
        totalWagered + amountWagered,
        totalWon
    );
  }

  public double getWinRate() {
    if (totalBets == 0) {
      return 0.0;
    }
    return (double) wonBets / totalBets * 100.0;
  }

  public double getNetProfit() {
    return totalWon - totalWagered;
  }

  public double getRankingScore() {
    double winRateComponent = getWinRate() * 0.6;
    double profitRatio = totalWagered > 0.0 ? (getNetProfit() / totalWagered) * 100.0 : 0.0;
    double profitComponent = profitRatio * 0.4;
    return winRateComponent + profitComponent;
  }

  public String toDisplayString() {
    return String.format(
        "%s | Apuestas: %d | V: %d | D: %d | WinRate: %.1f%% | Profit: $%.2f | Score: %.2f",
        clientId, totalBets, wonBets, lostBets, getWinRate(), getNetProfit(), getRankingScore()
    );
  }
}
