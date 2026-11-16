package domain;

public class NormalEffect implements Effect {
    public NormalEffect() {
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public boolean canKick() {
        return true;
    }

    @Override
    public int getSpeed() {
        return 1;
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
