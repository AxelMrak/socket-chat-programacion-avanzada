package com.tpsockets.domain.bet;

public interface BetLogRepository {
  void append(BetEntry betEntry);
}
