package domain;

public abstract class Power {
    public abstract Effect createEffect();
    public abstract int getDamage();            // base damage from Annex 1

    public String getName() {
        return getClass().getSimpleName();
    }

    public float getShotSpeed() { return 14f; }

    public void launchBall(Ball ball, int direction) {
        if (ball == null) return;
        ball.setVelocity(direction * getShotSpeed(), 0.0f);
    }

    public void updateBallFlight(Ball ball) {
        // Default: normal gravity applies
    }

    public int getDecoyBallCount() {
        return 0;
    }

    public float getDecoyDurationSeconds() {
        return 0.0f;
    }

    public void use(Player owner, Player target) {
        if (target == null) return;
        target.takeDamage(getDamage() * owner.getKickMultiplier());   // DamageShot x kickMultiplier
        target.applyEffect(createEffect());
    }
}
