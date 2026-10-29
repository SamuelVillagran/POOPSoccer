package domain;

public class Goal extends ItemSoccer {
    private Side side;
    private Player owner;

    public Goal(Side side, Player owner) {
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 0.0f;
    }

    @Override
    public Vector2D getPosition() {
        return null;
    }

    public Side getSide() {
        return null;
    }
}
