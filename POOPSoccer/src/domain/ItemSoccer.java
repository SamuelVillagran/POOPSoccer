package domain;

public abstract class ItemSoccer implements Element {
    protected Vector2D position;
    @Override
    public float getWidth() {
        return 50.0f;
    }

    @Override
    public float getHeight() {
        return 50.0f;
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }
}
