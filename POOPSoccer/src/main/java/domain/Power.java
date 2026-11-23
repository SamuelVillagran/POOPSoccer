package domain;

import java.io.Serializable;

public interface Power extends Serializable { /*Perfeccionado con Antigravity.*/
    Effect createEffect();
    int getDamage(); // Daño base del tiro especial definido en el Anexo 1

    default String getName() {
        return getClass().getSimpleName();
    }

    default float getShotSpeed() { return 14f; } // Rapidez horizontal base por defecto al lanzar el balón

    default void launchBall(Ball ball, int direction) {
        if (ball == null) throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "ball");
        ball.setVelocity(direction * getShotSpeed(), 0.0f); // Dispara el balón recto hacia el arco contrario
    }

    default void updateBallFlight(Ball ball) {} // Conserva la gravedad estándar salvo que una subclase altere el vuelo

    default int getDecoyBallCount() {
        return 0; // Por defecto no genera balones señuelo adicionales
    }

    default float getDecoyDurationSeconds() {
        return 0.0f;
    }

    default boolean showsShotAura() {
        return getDecoyBallCount() == 0; // Oculta el aura para no delatar cuál es el balón real en Ilusión
    }

    /**
     * Returns the visual effect sprite key associated with this power shot, or empty if none.
     */
    default String getShotEffectSprite() {
        return "";
    }

    default void use(Player owner, Player target) {
        if (target == null) throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER);
        target.takeDamage(getDamage() * owner.getKickMultiplier()); // Aplica el daño base multiplicado por Kick
        target.applyEffect(createEffect());
    }
}
