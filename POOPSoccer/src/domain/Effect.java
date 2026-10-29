package domain;

public interface Effect {
    boolean canJump();
    boolean canKick();
    float getSpeed();
    float getSize();
    boolean isDirectionInverted();
    void update(float deltaTime);
}
