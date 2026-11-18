package presentation;

/**
 * Abstraction for playing short non-blocking sound effects concurrently with background music.
 */
public interface SoundEffectPlayer {

    /**
     * Plays the given sound effect once without interrupting background music.
     */
    void play(SoundEffect effect);
}
