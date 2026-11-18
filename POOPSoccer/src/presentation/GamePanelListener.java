package presentation;

public interface GamePanelListener {
    void goToMenu();
    void restartFromLevel1();
    void onMatchInfoChanged(MatchHudSnapshot hud);
    void onMatchFinished(MatchResultSummary summary);
}
