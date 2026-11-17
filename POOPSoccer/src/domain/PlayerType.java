package domain;

import java.io.Serializable;

public abstract class PlayerType implements Serializable {
    private Player player;
    private Power power;

    public PlayerType() {
    }

    public void getSpeed() {
    }

    public void getPower() {
    }
}
