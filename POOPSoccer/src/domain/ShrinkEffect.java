package domain;

public class ShrinkEffect implements Effect {
    private float timer;

    public ShrinkEffect() {
    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public boolean canKick() {
        return false;
    }

    @Override
    public int getSpeed() {
        return 1;
    }

    @Override
    public float getSize() {
        return 0.6f;
    }

    @Override
    public boolean isDirectionInverted() {
        return false;
    }

    @Override
    public void update(float deltaTime) {
    }
}
