package presentation;

import java.util.Objects;

/**
 * Immutable presentation descriptor for a selectable game mode and its machine-controlled slots.
 */
public final class GameModeOption {

    private final String id;
    private final String displayName;
    private final boolean player1Machine;
    private final boolean player2Machine;

    public GameModeOption(String id, String displayName, boolean player1Machine, boolean player2Machine) {
        this.id = id;
        this.displayName = displayName;
        this.player1Machine = player1Machine;
        this.player2Machine = player2Machine;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isMachine(int playerNum) {
        return playerNum == 1 ? player1Machine : playerNum == 2 && player2Machine;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GameModeOption other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(displayName, other.displayName)
                && player1Machine == other.player1Machine
                && player2Machine == other.player2Machine;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, player1Machine, player2Machine);
    }

    @Override
    public String toString() {
        return "GameModeOption[id=" + id
                + ", displayName=" + displayName
                + ", player1Machine=" + player1Machine
                + ", player2Machine=" + player2Machine + "]";
    }
}
