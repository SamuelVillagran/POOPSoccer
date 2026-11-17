package domain;

public abstract class ItemSoccer implements Element {
    protected Vector2D position = new Vector2D();
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

    public void setPositions(float x, float y) {
        position.setPositions(x, y);
    }
}
