package presentation;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Almacena los parámetros de configuración seleccionados en los menús
 * antes de instanciar e iniciar el partido.
 */
public class GameSetup {

    private GameMode gameMode;
    private MatchType matchType;

    private String nameP1;
    private String teamP1;

    private String nameP2;
    private String teamP2;

    public GameSetup() {
        this.gameMode = GameMode.PVM;
        this.matchType = MatchType.PER_GOALS;
    }

    public void setMode(GameMode mode) {
        this.gameMode = mode;
        switch (mode) {
            case PVP -> this.gameMode = GameMode.PVP;
            case PVM -> this.gameMode = GameMode.PVM;
            case MVM -> this.gameMode = GameMode.MVM;
            default -> this.gameMode = GameMode.PVM;
        }
    }

    public GameMode getMode() {
        return gameMode;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public MatchType getMatchType() {
        return matchType;
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
}
