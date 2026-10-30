package domain;

import java.util.Map;

public class POOPSoccer {
    private static POOPSoccer game;
    private boolean isPaused;

    private int currentLevel;
    private CollisionChecker cChecker;
    private ScoreBoard infoMatch;
    private Map elements;

    private POOPSoccer() {
    }

    public static POOPSoccer getGame() {
        if (game == null) {
            game = new POOPSoccer();
        }
        return game;
    }

    public void startGame(GameMode gameMode, MatchType matchType, int goalLimit,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {


    }

    public void resumeGame() {
        isPaused = true;
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
