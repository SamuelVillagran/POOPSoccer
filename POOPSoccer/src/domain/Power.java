package domain;

public abstract class Power {
    public abstract Effect createEffect();
    public abstract int getDamage();            // base damage from Annex 1

    public float getShotSpeed() { return 14f; }

    public void use(Player owner, Player target) {
        if (target == null) return;
        target.takeDamage(getDamage() * owner.getKickMultiplier());   // DamageShot x kickMultiplier
        target.applyEffect(createEffect());
    }
}
