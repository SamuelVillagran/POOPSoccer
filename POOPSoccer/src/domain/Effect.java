package domain;

public interface Effect {
    boolean canJump();
    boolean canKick();
    int getSpeed();
    float getSize();
    boolean isDirectionInverted();
    void update(float deltaTime);
    default boolean isFinished() { return false; }
    default boolean isPushed() { return false; }
    default float getSpeedFactor() { return getSpeed(); }
}
