package presentation;

/**
 * Abstraction for playing short non-blocking sound effects concurrently with background music.
 */
public interface SoundEffectPlayer { /*Perfeccionado con Antigravity.*/

    /**
     * Plays the given sound effect once without interrupting background music.
     */
    void play(SoundEffect effect);

    /**
     * Plays the sound matching the given event key once without interrupting background music.
     */
    default void play(String soundKey) {
        SoundEffect.fromKey(soundKey).ifPresent(this::play);
    }
}
