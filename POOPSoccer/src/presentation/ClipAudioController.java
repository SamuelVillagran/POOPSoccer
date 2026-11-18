package presentation;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Java Sound (Clip) implementation of AudioController managing lifecycle, volume, and track transitions.
 */
public class ClipAudioController implements AudioController {

    private static final float[] VOLUME_DECIBELS = { -80f, -20f, -12f, -5f, 1f, 6f }; // Escala discreta de ganancia de silencio a máximo

    private Clip activeClip; // Pista de audio actualmente abierta en la tarjeta de sonido
    private LineListener activeListener;
    private SoundTrack currentTrack;
    private boolean looping; // Indica si la pista actual se repite de forma continua
    private int volumeScale = 4; // Nivel de volumen inicial (índice sobre VOLUME_DECIBELS)
    private long playbackGeneration = 0; // Evita que eventos STOP de pistas viejas cierren la pista recién iniciada

    @Override
    public synchronized void playLoop(SoundTrack track) {
        if (track == null) {
            return;
        }
        if (currentTrack == track && looping && isPlaying()) {
            return; // Conserva la reproducción continua si ya suena la misma música
        }
        startClip(track, true, null);
    }

    @Override
    public synchronized void playOnce(SoundTrack track) {
        if (track == null) {
            return;
        }
        startClip(track, false, null);
    }

    @Override
    public synchronized void playOnceThenLoop(SoundTrack onceTrack, SoundTrack followUpLoopTrack) {
        if (onceTrack == null) {
            return;
        }
        startClip(onceTrack, false, () -> playLoop(followUpLoopTrack)); // Encadena automáticamente el bucle al terminar el tema corto
    }

    @Override
    public synchronized void stop() {
        playbackGeneration++; // Invalida cualquier oyente pendiente de la generación anterior
        closeActiveClip();
        currentTrack = null;
        looping = false;
    }

    @Override
    public synchronized boolean isPlaying() {
        return activeClip != null && activeClip.isRunning();
    }

    @Override
    public synchronized SoundTrack getCurrentTrack() {
        return currentTrack;
    }

    public synchronized int getVolumeScale() {
        return volumeScale;
    }

    public synchronized void setVolumeScale(int scale) {
        this.volumeScale = Math.max(0, Math.min(VOLUME_DECIBELS.length - 1, scale)); // Acota el índice de volumen al rango válido
        applyVolume(activeClip);
    }

    private void startClip(SoundTrack track, boolean loopContinuously, Runnable onNaturalCompletion) {
        long generation = ++playbackGeneration;
        closeActiveClip();

        URL resourceUrl = getClass().getResource(track.getResourcePath());
        if (resourceUrl == null) {
            System.err.println("No se encontró el recurso de audio: " + track.getResourcePath());
            return;
        }

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(resourceUrl)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            applyVolume(clip);

            this.activeClip = clip;
            this.currentTrack = track;
            this.looping = loopContinuously;

            if (loopContinuously) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                registerCompletionListener(clip, generation, onNaturalCompletion);
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("No se pudo reproducir la pista " + track + ": " + e.getMessage());
            closeActiveClip();
            this.currentTrack = null;
            this.looping = false;
        }
    }

    private void registerCompletionListener(Clip clip, long generation, Runnable onNaturalCompletion) {
        activeListener = event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                handleClipStopped(generation, onNaturalCompletion);
            }
        };
        clip.addLineListener(activeListener);
    }

    private synchronized void handleClipStopped(long generation, Runnable onNaturalCompletion) {
        if (generation != this.playbackGeneration) {
            return; // Ignora cierres tardíos cuando ya arrancó otra pista más reciente
        }
        closeActiveClip();
        currentTrack = null;
        looping = false;
        if (onNaturalCompletion != null) {
            onNaturalCompletion.run();
        }
    }

    private void applyVolume(Clip clip) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float targetDb = VOLUME_DECIBELS[volumeScale];
        float clampedDb = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), targetDb)); // Respeta límites del driver
        gainControl.setValue(clampedDb);
    }

    private void closeActiveClip() {
        if (activeClip != null) {
            try {
                if (activeListener != null) {
                    activeClip.removeLineListener(activeListener);
                }
                if (activeClip.isRunning()) {
                    activeClip.stop();
                }
                activeClip.close();
            } catch (Exception ignored) {
            } finally {
                activeClip = null;
                activeListener = null;
            }
        }
    }
}
