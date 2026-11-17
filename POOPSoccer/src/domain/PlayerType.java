package domain;

import java.io.Serializable;

/**
 * Base class that defines whether a player is controlled by a person or by the machine.
 */
public abstract class PlayerType implements Serializable {
    protected Player player;

    public PlayerType() {
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void action() {
        // Por defecto un jugador humano no ejecuta acciones automáticas
    }
}
