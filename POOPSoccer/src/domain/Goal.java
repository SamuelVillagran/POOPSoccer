package domain;

public class Goal extends ItemSoccer {

    public Goal() {
    }

    @Override
    public float getWidth() {
        return 45.0f;
    }

    @Override
    public float getHeight() { return 115.0f; }

    @Override
    public boolean isMovible(){
        return false;
    }

    @Override
    public boolean hasCollision() {
        return false;
    }
}
