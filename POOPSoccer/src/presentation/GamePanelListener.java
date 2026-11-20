package presentation;

public interface GamePanelListener { /*Perfeccionado con Antigravity.*/
    void goToMenu();
    void restartFromLevel1();
    void onMatchInfoChanged(MatchHudSnapshot hud);
    void onMatchFinished(MatchResultSummary summary);
}
