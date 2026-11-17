package domain;

import java.io.Serializable;

public class Vector2D implements Serializable {
    private float x;
    private float y;

    public Vector2D() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPositions(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    public void add(Vector2D vector) {
        if (vector != null) {
            this.x += vector.getX();
            this.y += vector.getY();
        }
    }

    public void scale(float factor) {
        this.x *= factor;
        this.y *= factor;
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2D normalize() {
        float mag = magnitude();
        if (mag == 0.0f) {
            return new Vector2D(0.0f, 0.0f);
        }
        return new Vector2D(x / mag, y / mag);
    }
}
