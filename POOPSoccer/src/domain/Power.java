package domain;

public abstract class Power {
    public abstract Effect createEffect();

    public void use(Player owner, Player target) {
    }
}
