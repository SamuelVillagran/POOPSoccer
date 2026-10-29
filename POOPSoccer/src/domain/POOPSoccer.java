package domain;

import java.util.Map;

public class POOPSoccer {
    private static POOPSoccer game;

    private int currentLevel;
    private CollisionChecker cChecker;
    private ScoreBoard infoMatch;
    private Map elements;

    private POOPSoccer() {
    }

    public static POOPSoccer getGame() {
        return game;
    }

    public void startGame(int gameMode, int currentLevel) {
    }

    public Map getElementsToDraw() {
        return null;
    }

    public Player getPlayer1() {
        return null;
    }

    public Player getPlayer2() {
        return null;
    }
}
