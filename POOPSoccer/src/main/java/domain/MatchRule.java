package domain;

import java.io.Serializable;

/**
 * Strategy contract defining match duration updates and finish conditions.
 */
public interface MatchRule extends Serializable { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/

    /**
     * Advances time-dependent rule state by one frame.
     */
    default void update(float deltaTime, Player player1, Player player2) {
    }

    /**
     * Returns true when the match has satisfied its end condition.
     */
    boolean isFinished(Player player1, Player player2);

    /**
     * Returns remaining time in seconds for HUD display, or 0 for non-timed rules.
     */
    default float getRemainingTime() {
        return 0.0f;
    }

    /**
     * Returns true when the match is in sudden death tie-breaker mode.
     */
    default boolean isSuddenDeath() {
        return false;
    }
}
