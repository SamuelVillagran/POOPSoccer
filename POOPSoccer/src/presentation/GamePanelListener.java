package presentation;

public interface GamePanelListener {
    void goToMenu();
    void restartFromLevel1();
    void onMatchInfoChanged(String p1Info, String score, float timeRemaining, String p2Info, float hp1, float hp2);

    default void onMatchInfoChanged(String p1Info, String score, float timeRemaining, String p2Info,
                                    float hp1, float hp2, float power1, float power2,
                                    String activePower1, String activePower2) {
        onMatchInfoChanged(p1Info, score, timeRemaining, p2Info, hp1, hp2);
    }
}
