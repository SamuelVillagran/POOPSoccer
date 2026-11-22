package presentation;

/**
 * Abstraction for controlling background music and sound effect playback in the presentation layer.
 */
public interface AudioController { /*Perfeccionado con Antigravity.*/

    /**
     * Plays the specified track in a continuous loop until stopped or replaced.
     */
    void playLoop(SoundTrack track);

    /**
     * Plays the primary track once and automatically transitions to looping the follow-up track upon completion.
     */
    void playOnceThenLoop(SoundTrack onceTrack, SoundTrack followUpLoopTrack);
}
