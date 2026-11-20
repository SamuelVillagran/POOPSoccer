package domain;

import java.io.Serializable;

/**
 * Defines a match mode strategy responsible for player control types, default names, and manual input permissions.
 */
public abstract class TypeMatch implements Serializable { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/

    /**
     * Resolves a TypeMatch instance from a game mode name.
     */
    public static TypeMatch fromName(String modeName) {
        return GameMode.fromName(modeName).createTypeMatch();
    }

    protected abstract Type createPlayer1Type();

    protected abstract Type createPlayer2Type();

    public abstract boolean isPlayer1Human();

    public abstract boolean isPlayer2Human();

    protected String getDefaultPlayer1Name() {
        return isPlayer1Human() ? "Jugador 1" : "CPU 1";
    }

    protected String getDefaultPlayer2Name() {
        return isPlayer2Human() ? "Jugador 2" : "CPU";
    }

    /**
     * Builds Player 1 configured according to this match mode.
     */
    public Player createPlayer1(String name, Team team) {
        return createPlayer(createPlayer1Type(), name, getDefaultPlayer1Name(), team, NumberPlayer.PLAYER1);
    }

    /**
     * Builds Player 2 configured according to this match mode.
     */
    public Player createPlayer2(String name, Team team) {
        return createPlayer(createPlayer2Type(), name, getDefaultPlayer2Name(), team, NumberPlayer.PLAYER2);
    }

    private Player createPlayer(Type controlType, String providedName, String defaultName,
                                Team team, NumberPlayer numberPlayer) {
        if (controlType == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "controlType");
        }
        if (team == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "team");
        }
        String resolvedName = (providedName != null && !providedName.isBlank()) ? providedName : defaultName;
        return new Player(controlType, resolvedName, team, numberPlayer);
    }

    /**
     * Returns true when the given player slot accepts manual keyboard input.
     */
    public boolean canControlManually(int playerNum) {
        if (playerNum == 1) return isPlayer1Human();
        if (playerNum == 2) return isPlayer2Human();
        return false;
    }

    /**
     * Returns true when the given player slot is controlled by the machine.
     */
    public boolean isMachinePlayer(int playerNum) {
        if (playerNum == 1) return !isPlayer1Human();
        if (playerNum == 2) return !isPlayer2Human();
        return false;
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
