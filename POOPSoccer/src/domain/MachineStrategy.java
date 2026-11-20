package domain;

import java.io.Serializable;

/**
 * Decision-making behavior of a machine player. Each machine profile provides its own implementation.
 */
public interface MachineStrategy extends Serializable { /*Perfeccionado con Antigravity.*/

    /**
     * Called once per frame to let the strategy drive the given player.
     */
    void act(Player player);
}
