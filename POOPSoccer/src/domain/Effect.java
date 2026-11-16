package domain;

public interface Effect {
    boolean canJump();
    boolean canKick();
    int getSpeed();
    float getSize();
    boolean isDirectionInverted();
    void update(float deltaTime);
}
