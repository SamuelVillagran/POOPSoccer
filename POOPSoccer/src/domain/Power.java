package domain;

import java.io.Serializable;

public abstract class Power implements Serializable { /*Perfeccionado con Antigravity.*/
    public abstract Effect createEffect();
    public abstract int getDamage(); // Daño base del tiro especial definido en el Anexo 1

    public String getName() {
        return getClass().getSimpleName();
    }

    public float getShotSpeed() { return 14f; } // Rapidez horizontal base por defecto al lanzar el balón

    public void launchBall(Ball ball, int direction) {
        if (ball == null) throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "ball");
        ball.setVelocity(direction * getShotSpeed(), 0.0f); // Dispara el balón recto hacia el arco contrario
    }

    public void updateBallFlight(Ball ball) {} // Conserva la gravedad estándar salvo que una subclase altere el vuelo

    public int getDecoyBallCount() {
        return 0; // Por defecto no genera balones señuelo adicionales
    }

    public float getDecoyDurationSeconds() {
        return 0.0f;
    }

    public boolean showsShotAura() {
        return getDecoyBallCount() == 0; // Oculta el aura para no delatar cuál es el balón real en Ilusión
    }

    /**
     * Returns the visual effect sprite key associated with this power shot, or empty if none.
     */
    public String getShotEffectSprite() {
        return "";
    }

    public void use(Player owner, Player target) {
        if (target == null) throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER);
        target.takeDamage(getDamage() * owner.getKickMultiplier()); // Aplica el daño base multiplicado por Kick
        target.applyEffect(createEffect());
    }
}
