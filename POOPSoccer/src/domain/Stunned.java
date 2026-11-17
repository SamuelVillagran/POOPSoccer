package domain;

public class Stunned implements Effect {
    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public boolean canKick() {
        return false;
    }
}
