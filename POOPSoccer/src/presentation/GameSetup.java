package presentation;

import domain.GameMode;
import domain.MatchType;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Almacena los parámetros de configuración seleccionados en los menús
 * antes de instanciar e iniciar el partido.
 */
public class GameSetup {

    private GameMode gameMode;
    private MatchType matchType;
    private int goalLimit;
    private int timeLimit; // in minutes, used when matchType == PER_TIME

    private String nameP1;
    private String teamP1;

    private String nameP2;
    private String teamP2;

    public GameSetup() {
        this.gameMode = GameMode.PVM;
        this.matchType = MatchType.PER_GOALS;
        this.goalLimit = 3;
        this.timeLimit = 2;
    }

    public void setMode(GameMode mode) {
        this.gameMode = mode;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setGoalLimit(int goalLimit) {
        this.goalLimit = goalLimit;
    }

    public int getGoalLimit() {
        return goalLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setPlayer1(String name, String team) {
        this.nameP1 = name;
        this.teamP1 = team;
    }

    public void setPlayer2(String name, String team) {
        this.nameP2 = name;
        this.teamP2 = team;
    }

    public String getNameP1() { return nameP1; }
    public String getTeamP1() { return teamP1; }
    public String getNameP2() { return nameP2; }
    public String getTeamP2() { return teamP2; }
    public GameMode getGameMode() { return gameMode; }
}
