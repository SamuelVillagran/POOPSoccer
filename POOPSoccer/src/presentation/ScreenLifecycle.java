package presentation;

/**
 * Optional lifecycle hooks invoked by GameContainer when a screen is shown or hidden.
 */
public interface ScreenLifecycle {

    default void onScreenShown() {
    }

    default void onScreenHidden() {
    }
}
