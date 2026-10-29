package domain;

public class Pitch extends ItemSoccer {
    private Side side;

    public Pitch(Side side) {
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 0.0f;
    }

    public Side getSide() {
        return null;
    }

    @Override
    public Vector2D getPosition() {
        return null;
    }
}
