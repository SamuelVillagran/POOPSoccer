package domain;

public abstract class ItemSoccer implements Element {
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
