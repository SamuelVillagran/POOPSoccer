package presentation;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

/**
 * Java Sound implementation of SoundEffectPlayer that preloads PCM buffers in memory
 * so sound effects play immediately at full volume without blocking the game loop.
 */
public class EffectPlayer implements SoundEffectPlayer { /*Perfeccionado con Antigravity.*/

    private static final float TARGET_GAIN_DB = 6.0f; // Ganancia máxima en decibelios para destacar los efectos sobre la música

    private final Map<String, CachedAudio> cache = new ConcurrentHashMap<>();

    public EffectPlayer() {
        preloadAll();
    }

    /**
     * Registers and preloads a sound effect under the specified key without modifying EffectPlayer.
     */
    public boolean registerSound(String soundKey, String resourcePath) {
        return loadAndCacheSound(soundKey, resourcePath);
    }

    private boolean loadAndCacheSound(String soundKey, String resourcePath) {
        if (soundKey == null || soundKey.isBlank()) {
            throw new IllegalArgumentException("Sound key cannot be blank.");
        }
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Sound resource path cannot be blank.");
        }
        CachedAudio loaded = loadAudio(soundKey, resourcePath);
        if (loaded == null) {
            return false;
        }
        cache.put(normalizeKey(soundKey), loaded);
        return true;
    }

    public boolean hasSound(String soundKey) {
        return soundKey != null && !soundKey.isBlank() && cache.containsKey(normalizeKey(soundKey));
    }

    @Override
    public void play(SoundEffect effect) {
        if (effect != null) {
            play(effect.name());
        }
    }

    @Override
    public void play(String soundKey) {
        if (soundKey == null || soundKey.isBlank()) {
            return;
        }
        CachedAudio audio = cache.get(normalizeKey(soundKey));
        if (audio == null) {
            return;
        }
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close(); // Libera la línea de audio nativa apenas termina de sonar el efecto
                }
            });
            clip.open(audio.getFormat(), audio.getPcmData(), 0, audio.getPcmData().length); // Abre desde memoria para evitar latencia de disco
            applyMaxVolume(clip);
            clip.start();
        } catch (Exception e) {
            System.err.println("No se pudo reproducir el efecto " + soundKey + ": " + e.getMessage());
        }
    }

    private void preloadAll() {
        for (SoundEffect effect : SoundEffect.values()) {
            loadAndCacheSound(effect.name(), effect.getResourcePath());
        }
    }

    private CachedAudio loadAudio(String soundKey, String resourcePath) {
        try (InputStream rawStream = getClass().getResourceAsStream(resourcePath)) {
            if (rawStream == null) {
                System.err.println("No se encontró el efecto de sonido: " + resourcePath);
                return null;
            }
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    new java.io.BufferedInputStream(rawStream))) { // Envuelve en búfer para soportar mark/reset al leer WAV
                AudioFormat format = audioStream.getFormat();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[4096];
                int read;
                while ((read = audioStream.read(chunk)) != -1) {
                    buffer.write(chunk, 0, read);
                }
                return new CachedAudio(format, buffer.toByteArray());
            }
        } catch (Exception e) {
            System.err.println("No se pudo precargar el efecto " + soundKey + ": " + e.getMessage());
            return null;
        }
    }

    private static String normalizeKey(String soundKey) {
        return soundKey.trim().toUpperCase(Locale.ROOT);
    }

    private void applyMaxVolume(Clip clip) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), TARGET_GAIN_DB));
        gain.setValue(clamped);
    }

    /**
     * Stores the audio format and decoded PCM bytes in memory for immediate effect playback.
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
