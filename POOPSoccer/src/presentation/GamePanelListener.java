package presentation;

public interface GamePanelListener {
    void goToMenu();
    void restartFromLevel1();
    void onMatchInfoChanged(String p1Info, String score, float timeRemaining, boolean suddenDeath,
                            String p2Info, float hp1, float hp2,
                            float power1, float power2,
                            String activePower1, String activePower2);
}
