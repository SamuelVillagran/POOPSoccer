package domain;

public interface Element {
    float getWidth();
    float getHeight();
    Vector2D getPosition();
    boolean isMovible();

    default boolean hasCollision() {
        return true;
    }

    default float getX() {
        return getPosition() != null ? getPosition().getX() : 0.0f;
    }

    default float getY() {
        return getPosition() != null ? getPosition().getY() : 0.0f;
    }
}
