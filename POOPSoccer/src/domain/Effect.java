package domain;

import java.io.Serializable;

public interface Effect extends Serializable { /*Perfeccionado con Antigravity.*/
    default boolean canJump() { return true; } // Indica si el jugador afectado conserva la capacidad de saltar
    default boolean canKick() { return true; } // Indica si el jugador afectado conserva la capacidad de patear
    default void update(float deltaTime) {} // Descuenta el tiempo restante del efecto en cada frame
    default boolean isFinished() { return false; } // Señala si el efecto ya expiró y debe retirarse
    default boolean isPushed() { return false; } // Indica si el efecto empuja al jugador hacia su propio arco
    default boolean launchesIntoAir() { return false; } // Indica si eleva al jugador al recibir el impacto
    default float getSpeedFactor() { return 1.0f; } // Factor multiplicador aplicado a la velocidad normal (1.0 = 100%)
    default void onApply(Player affected) {} // Hook ejecutado una vez al aplicar el efecto sobre el jugador
    default void onTick(Player affected, float deltaTime) {} // Hook ejecutado en cada frame mientras el efecto sigue activo
    default void onExpire(Player affected) {} // Hook ejecutado una vez cuando el efecto finaliza y se retira
}
