package domain;

public class UnconsciousEffect extends TimedEffect {    // Nigeria
    public UnconsciousEffect() { super(3f); }
    @Override public boolean canJump() { return false; }
    @Override public boolean canKick() { return false; }
    @Override public int getSpeed() { return 0; }
    @Override public boolean launchesIntoAir() { return true; }
}