package com.tpsockets.domain.bet;

public class BetSelectionState {

  public enum Step {
    IDLE,
    SELECTING_MATCH,
    PLACING_BET
  }

  private Step step = Step.IDLE;
  private Integer selectedMatchId;

  public Step step() {
    return step;
  }

  public Integer selectedMatchId() {
    return selectedMatchId;
  }

  public void startSelection() {
    step = Step.SELECTING_MATCH;
    selectedMatchId = null;
  }

  public void enterRoom(int matchId) {
    step = Step.PLACING_BET;
    selectedMatchId = matchId;
  }

  public void reset() {
    step = Step.IDLE;
    selectedMatchId = null;
  }
}
