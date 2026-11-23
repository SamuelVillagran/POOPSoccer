package domain;

import java.io.Serializable;

/**
 * Strategy contract that defines whether a player is controlled by a person or by the machine.
 */
public interface Type extends Serializable { /*Perfeccionado con Antigravity.*/
    default void setPlayer(Player player) {}

    default void action() {} // Conserva comportamiento vacío por defecto cuando el control es humano
}
