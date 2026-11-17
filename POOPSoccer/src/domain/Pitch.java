package domain;

public class Pitch extends ItemSoccer {

    private Side side;

    public Pitch(Side side) {
        this.side = side;
    }

    @Override
    public float getWidth() { return 45.0f; }

    @Override
    public float getHeight() { return 8.0f; }

    @Override public boolean isMovible() { return false; }

    public Side getSide() { return side; }
}
