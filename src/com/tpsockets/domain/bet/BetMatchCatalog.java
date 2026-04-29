package com.tpsockets.domain.bet;

import java.util.List;
import java.util.Optional;

public class BetMatchCatalog {

  private final List<BetMatch> matches =
      List.of(
          new BetMatch(1, "Argentina vs Uruguay - 03/05/2026", List.of("Argentina", "Uruguay")),
          new BetMatch(2, "Argentina vs Chile - 11/05/2026", List.of("Argentina", "Chile")),
          new BetMatch(3, "Argentina vs Colombia - 24/05/2026", List.of("Argentina", "Colombia")));

  public List<BetMatch> listAll() {
    return matches;
  }

  public Optional<BetMatch> findById(int id) {
    return matches.stream().filter(match -> match.id() == id).findFirst();
  }
}
