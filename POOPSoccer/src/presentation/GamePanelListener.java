package presentation;

public interface GamePanelListener {
    void goToMenu();
    void restartFromLevel1();
    void onMatchInfoChanged(String p1Info, String score, float timeRemaining, String p2Info, float hp1, float hp2);
}
