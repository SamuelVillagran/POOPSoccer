package presentation;

/**
 * Abstraction for controlling background music and sound effect playback in the presentation layer.
 */
public interface AudioController {

    /**
     * Plays the specified track in a continuous loop until stopped or replaced.
     */
    void playLoop(SoundTrack track);

    /**
     * Plays the specified track once from the beginning.
     */
    void playOnce(SoundTrack track);

    /**
     * Plays the primary track once and automatically transitions to looping the follow-up track upon completion.
     */
    void playOnceThenLoop(SoundTrack onceTrack, SoundTrack followUpLoopTrack);

    /**
     * Stops any currently playing track and releases its underlying audio resources.
     */
    void stop();

    /**
     * Returns true if a track is currently active and playing.
     */
    boolean isPlaying();

    /**
     * Returns the currently loaded or playing track, or null if stopped.
     */
    SoundTrack getCurrentTrack();
}
