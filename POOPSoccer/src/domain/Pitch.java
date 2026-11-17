package domain;

public class Pitch extends ItemSoccer {

    public Pitch() {
    }

    @Override
    public float getWidth() { return 45.0f; }

    @Override
    public float getHeight() { return 8.0f; }

    @Override public boolean isMovible() { return false; }
}
