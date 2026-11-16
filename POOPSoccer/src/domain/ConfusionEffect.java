package domain;

public class ConfusionEffect implements Effect {
    private float timer;

    public ConfusionEffect() {
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
        return 0;
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
