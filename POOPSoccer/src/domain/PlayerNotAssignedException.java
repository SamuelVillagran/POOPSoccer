package domain;

/**
 * Thrown when a control strategy (Type) is used before being bound to a Player.
 */
public class PlayerNotAssignedException extends IllegalStateException {

    public PlayerNotAssignedException() {
        super("The control strategy has no player assigned. Call setPlayer(player) before action().");
    }
}
