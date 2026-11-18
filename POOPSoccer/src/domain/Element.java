package domain;

import java.io.Serializable;

public interface Element extends Serializable {
    float getWidth();
    float getHeight();
    Vector2D getPosition();
    boolean isMovible(); // Distingue elementos dinámicos (balón, jugadores) de obstáculos fijos (arcos, postes)

    /**
     * Advances the internal state of this element by one game frame.
     */
    default void update() {
    }

    default boolean hasCollision() {
        return true; // Indica si rebota contra otros elementos sólidos (false en la zona interior del arco)
    }

    default float getX() {
        return getPosition() != null ? getPosition().getX() : 0.0f;
    }

    default float getY() {
        return getPosition() != null ? getPosition().getY() : 0.0f;
    }

    default String getVariantName() {
        return "normal"; // Nombre de variante de sprite usado por defecto en presentación
    }

    default boolean hasPowerAura() {
        return false;
    }

    default boolean hasShotAura() {
        return false;
    }

    default boolean isStunnedStatus() {
        return false;
    }

    default String getStatusText() {
        return "";
    }
}
