package domain;

import java.io.Serializable;

/**
 * Base class that defines whether a player is controlled by a person or by the machine.
 */
public abstract class Type implements Serializable {
    protected Player player; // Referencia al jugador controlado por esta estrategia

    public Type() {
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void action() {} // Conserva comportamiento vacío por defecto cuando el control es humano
}
