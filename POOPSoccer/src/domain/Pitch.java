package domain;

public class Pitch extends ItemSoccer {

    private Side side;
    private float width;
    private float height;

    public Pitch(Side side) {
        this.side = side;
        this.position = new Vector2D();
    }

    @Override
    public float getWidth() { return 45.0f; }

    @Override
    public float getHeight() { return 8.0f; }

    @Override
    public Vector2D getPosition() { return position; }

    public void setPositions(float x, float y) {
        position.setPositions(x, y);
    }

    @Override public boolean isMovible() { return false; }

    public Side getSide() { return side; }
}
