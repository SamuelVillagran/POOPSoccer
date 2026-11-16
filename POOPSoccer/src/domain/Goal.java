package domain;

public class Goal extends ItemSoccer {
    private Side side;
    private Player owner;

    public Goal(Side side, Player owner) {
        this.side = side;
        this.owner = owner;

    }

    @Override
    public float getWidth() {
        return 45.0f;
    }

    @Override
    public float getHeight() { return 115.0f; }

    @Override
    public Vector2D getPosition() {
        return position;
    }

    @Override
    public boolean isMovible(){
        return false;
    }

    @Override
    public boolean hasCollision() {
        return false;
    }

    public void setPositions(float x, float y) {
        position.setPositions(x, y);
    }

    public Side getSide() {
        return side;
    }
}
