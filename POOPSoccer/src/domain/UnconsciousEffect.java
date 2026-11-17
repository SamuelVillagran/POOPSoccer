package domain;

public class UnconsciousEffect extends TimedEffect {    // Nigeria
    public UnconsciousEffect() { super(3f); }
    @Override public boolean canJump() { return false; }
    @Override public boolean canKick() { return false; }
    @Override public float getSpeedFactor() { return 0.0f; }
    @Override public boolean launchesIntoAir() { return true; }
}