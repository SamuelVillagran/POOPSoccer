package domain;

public class Ball extends ItemSoccer {
    private int weight;
    private Vector2D velocity;

    public Ball() {
    }

    public void move(Vector2D force, CollisionChecker cChecker) {
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
