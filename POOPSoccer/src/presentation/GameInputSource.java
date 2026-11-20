package presentation;

import java.awt.Component;

/**
 * Abstraction for polling player actions and pause triggers without coupling GamePanel to fixed keys.
 */
public interface GameInputSource {

    void dispatchInputs(GameController controller);

    boolean consumePauseTrigger();

    void clearInputs();

    default void attachTo(Component component) {
    }
}
