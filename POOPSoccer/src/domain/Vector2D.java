package domain;

public class Vector2D {
    private float x;
    private float y;

    public Vector2D() {
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void add(Vector2D vector) {
        float newXPos = x+vector.getX(),
                newYPos = y+vector.getY();
        setPositions(newXPos, newYPos);
    }

    public void setPositions(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }
}
