package domain;

public abstract class TimedEffect implements Effect {
    private float remaining;

    protected TimedEffect(float seconds) { this.remaining = seconds; }

    @Override
    public void update(float deltaTime) { remaining -= deltaTime; }

    @Override
    public boolean isFinished() { return remaining <= 0; }
}
