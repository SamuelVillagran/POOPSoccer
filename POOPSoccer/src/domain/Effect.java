package domain;

import java.io.Serializable;

public interface Effect extends Serializable {
    default boolean canJump() { return true; }
    default boolean canKick() { return true; }
    default int getSpeed() { return 1; }
    default float getSize() { return 0.0f; }
    default boolean isDirectionInverted() { return false; }
    default void update(float deltaTime) {}
    default boolean isFinished() { return false; }
    default boolean isPushed() { return false; }
    default boolean launchesIntoAir() { return false; }
    default float getSpeedFactor() { return getSpeed(); }
}
