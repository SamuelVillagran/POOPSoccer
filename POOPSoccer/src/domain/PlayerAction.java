package domain;

/**
 * Command representing an executable action that a Player can perform.
 */
@FunctionalInterface
public interface PlayerAction { /*Perfeccionado con Antigravity.*/
    void execute(Player player);
}
