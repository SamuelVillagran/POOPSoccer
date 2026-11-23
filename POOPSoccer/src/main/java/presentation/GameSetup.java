package presentation;

import java.awt.Dimension;

/**
 * Stores the configuration parameters selected in the menus before starting a match.
 */
public class GameSetup { /*Perfeccionado con Antigravity.*/

    private GameModeOption mode;
    private String matchType; // Regla de victoria elegida ("Por goles" o "Por tiempo")
    private int matchLimit;
    private Dimension resolution; // Dimensiones visuales seleccionadas para el panel de juego

    private String nameP1; // Nombre confirmado del primer participante
    private String teamP1; // País elegido para el primer participante

    private String nameP2; // Nombre confirmado del segundo participante o CPU
    private String teamP2; // País elegido para el segundo participante o CPU

    public GameSetup() {
        this.mode = new GameModeOption("PVM", "Jugador vs Máquina (PvM)", false, true);
        this.matchType = "Por goles";
        this.matchLimit = 3;
        this.resolution = new Dimension(800, 600);
        this.nameP1 = "Jugador 1";
        this.teamP1 = "Corea del Sur";
        this.nameP2 = "Máquina";
        this.teamP2 = "Camerún";
    }

    public void setResolution(Dimension resolution) {
        if (resolution != null) {
            this.resolution = resolution;
        }
    }

    public Dimension getResolution() {
        return resolution;
    }

    public void setMode(GameModeOption mode) {
        if (mode != null) {
            this.mode = mode;
        }
    }

    public GameModeOption getModeOption() {
        return mode;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchLimit(int matchLimit) {
        this.matchLimit = matchLimit;
    }

    public int getMatchLimit() {
        return matchLimit;
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
    public String getGameMode() { return mode.getId(); }
}
