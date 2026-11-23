package presentation;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Java Sound (Clip) implementation of AudioController managing lifecycle, caching, async loading, and transitions.
 */
public class MusicPlayer implements AudioController { /*Perfeccionado con Antigravity.*/

    private static final float TARGET_GAIN_DB = 1.0f; // Ganancia fija en decibelios para la música de fondo

    private final Map<SoundTrack, CachedAudio> cache = new ConcurrentHashMap<>(); // Evita releer pistas WAV pesadas del disco al cambiar de pantalla
    private final ExecutorService audioLoader = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "AudioLoader-Thread");
        thread.setDaemon(true); // Permite cerrar la JVM limpiamente sin bloquear la salida
        return thread;
    });

    private Clip activeClip; // Pista de audio actualmente abierta en la tarjeta de sonido
    private LineListener activeListener;
    private SoundTrack currentTrack;
    private boolean looping; // Indica si la pista actual se repite de forma continua
    private boolean loading = false; // Evita reiniciar la misma pista mientras termina de cargarse en segundo plano
    private long playbackGeneration = 0; // Evita que eventos STOP o cargas viejas pisen la pista recién iniciada

    @Override
    public synchronized void playLoop(SoundTrack track) {
        if (track == null) {
            return;
        }
        if (currentTrack == track && looping && (loading || isPlaying())) {
            return; // Conserva la reproducción continua si ya suena o se está cargando la misma música
        }
        startClip(track, true, null);
    }

    @Override
    public synchronized void playOnceThenLoop(SoundTrack onceTrack, SoundTrack followUpLoopTrack) {
        if (onceTrack == null) {
            return;
        }
        startClip(onceTrack, false, () -> playLoop(followUpLoopTrack)); // Encadena automáticamente el bucle al terminar el tema corto
    }

    private boolean isPlaying() {
        return activeClip != null && activeClip.isRunning();
    }

    private void startClip(SoundTrack track, boolean loopContinuously, Runnable onNaturalCompletion) {
        long generation = ++playbackGeneration;
        closeActiveClip();
        this.currentTrack = track;
        this.looping = loopContinuously;
        this.loading = true;
        audioLoader.submit(() -> loadAndStartClip(track, generation, loopContinuously, onNaturalCompletion)); // Carga fuera del hilo Swing
    }

    private void loadAndStartClip(SoundTrack track, long generation, boolean loopContinuously,
                                  Runnable onNaturalCompletion) {
        if (isStale(generation)) {
            return;
        }
        CachedAudio audio = cache.computeIfAbsent(track, this::loadAudio);
        if (audio == null || isStale(generation)) {
            clearIfCurrent(generation);
            return;
        }
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
            clip.open(audio.getFormat(), audio.getPcmData(), 0, audio.getPcmData().length);
            attachAndStartClip(clip, track, generation, loopContinuously, onNaturalCompletion);
        } catch (Exception e) {
            System.err.println("No se pudo reproducir la pista " + track + ": " + e.getMessage());
            closeClipQuietly(clip);
            clearIfCurrent(generation);
        }
    }

    private CachedAudio loadAudio(SoundTrack track) {
        try (InputStream rawStream = getClass().getResourceAsStream(track.getResourcePath())) {
            if (rawStream == null) {
                System.err.println("No se encontró el recurso de audio: " + track.getResourcePath());
                return null;
            }
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(rawStream))) {
                AudioFormat format = audioStream.getFormat();
                byte[] pcmData = audioStream.readAllBytes();
                return new CachedAudio(format, pcmData);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la pista " + track + ": " + e.getMessage());
            return null;
        }
    }

    private synchronized void attachAndStartClip(Clip clip, SoundTrack track, long generation,
                                                 boolean loopContinuously, Runnable onNaturalCompletion) {
        if (generation != this.playbackGeneration) {
            closeClipQuietly(clip); // Descarta el clip si el usuario cambió de pantalla o detuvo la música
            return;
        }
        applyVolume(clip);
        this.activeClip = clip;
        this.currentTrack = track;
        this.looping = loopContinuously;
        this.loading = false;

        if (loopContinuously) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            registerCompletionListener(clip, generation, onNaturalCompletion);
            clip.start();
        }
    }

    private synchronized boolean isStale(long generation) {
        return generation != this.playbackGeneration;
    }

    private synchronized void clearIfCurrent(long generation) {
        if (generation == this.playbackGeneration) {
            this.loading = false;
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
        loading = false;
        if (onNaturalCompletion != null) {
            onNaturalCompletion.run();
        }
    }

    private void applyVolume(Clip clip) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float clampedDb = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), TARGET_GAIN_DB)); // Respeta límites del driver
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

    private void closeClipQuietly(Clip clip) {
        if (clip == null) {
            return;
        }
        try {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Stores decoded PCM audio data in memory for instantaneous track transitions.
     */
    private static final class CachedAudio {

        private final AudioFormat format;
        private final byte[] pcmData;

        private CachedAudio(AudioFormat format, byte[] pcmData) {
            this.format = format;
            this.pcmData = (pcmData == null) ? new byte[0] : pcmData.clone();
        }

        private AudioFormat getFormat() {
            return format;
        }

        private byte[] getPcmData() {
            return pcmData;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CachedAudio other)) {
                return false;
            }
            return Objects.equals(format, other.format) && Arrays.equals(pcmData, other.pcmData);
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(format) + Arrays.hashCode(pcmData);
        }

        @Override
        public String toString() {
            return "CachedAudio[format=" + format + ", pcmDataLength=" + pcmData.length + "]";
        }
    }
}
