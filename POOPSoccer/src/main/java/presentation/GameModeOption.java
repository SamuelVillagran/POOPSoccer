package presentation;

/**
 * Immutable presentation descriptor for a selectable game mode and its machine-controlled slots.
 */
public record GameModeOption(String id, String displayName, boolean player1Machine, boolean player2Machine) {

    public boolean isMachine(int playerNum) {
        return playerNum == 1 ? player1Machine : playerNum == 2 && player2Machine;
    }
}
