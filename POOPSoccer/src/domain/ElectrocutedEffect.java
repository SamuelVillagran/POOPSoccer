package domain;

public class ElectrocutedEffect extends TimedEffect {   // Camerún
    public ElectrocutedEffect() { super(3f); }
    @Override public boolean canJump() { return false; }
    @Override public float getSpeedFactor() { return 0.5f; }
}