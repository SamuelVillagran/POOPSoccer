package domain;

public class Ball extends ItemSoccer {
    private int weight;
    private Vector2D velocity;
    private Player lastTouch;

    public Ball() {
    }

    public void move(Vector2D force) {
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
}
