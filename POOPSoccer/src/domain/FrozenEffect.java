package domain;

public class FrozenEffect implements Effect {
    private float timer;

    public FrozenEffect() {
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
    public float getSpeed() {
        return 0.0f;
    }

    @Override
    public float getSize() {
        return 0.0f;
    }
    @Override
    public boolean isDirectionInverted() {
        return false;
    }

    @Override
    public void update(float deltaTime) {
    }
}
