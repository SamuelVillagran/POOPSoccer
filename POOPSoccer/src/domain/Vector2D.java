package domain;

import java.io.Serializable;

public class Vector2D implements Serializable {
    private float x; // Componente horizontal del vector en píxeles
    private float y; // Componente vertical del vector (positivo hacia abajo)

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
            this.x += vector.getX(); // Suma componente X para actualizar velocidad o posición
            this.y += vector.getY(); // Suma componente Y en el plano de la cancha
        }
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y); // Calcula la norma euclidiana del vector
    }

    public Vector2D normalize() {
        float mag = magnitude();
        if (mag == 0.0f) {
            return new Vector2D(0.0f, 0.0f); // Evita división por cero cuando el vector es nulo
        }
        return new Vector2D(x / mag, y / mag); // Retorna un vector unitario con la misma dirección
    }
}
