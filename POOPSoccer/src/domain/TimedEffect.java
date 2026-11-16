package domain;

public abstract class TimedEffect implements Effect {
    private float remaining;

    protected TimedEffect(float seconds) { this.remaining = seconds; }

    @Override
    public void update(float deltaTime) { remaining -= deltaTime; }

    @Override
    public boolean isFinished() { return remaining <= 0; }

    @Override
    public boolean canJump() { return true; }

    @Override
    public boolean canKick() { return true; }

    @Override
    public int getSpeed() { return 1; }

    @Override
    public float getSize() { return 0.0f; }

    @Override
    public boolean isDirectionInverted() { return false; }
}
