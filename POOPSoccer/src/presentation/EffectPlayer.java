package presentation;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

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
public class EffectPlayer implements SoundEffectPlayer {

    private static final float TARGET_GAIN_DB = 6.0f; // Ganancia máxima en decibelios para destacar los efectos sobre la música

    private record CachedAudio(AudioFormat format, byte[] pcmData) { // Almacena el formato y los bytes PCM decodificados en RAM
    }

    private final Map<SoundEffect, CachedAudio> cache = new EnumMap<>(SoundEffect.class); // Caché rápida indexada por enum

    public EffectPlayer() {
        preloadAll();
    }

    @Override
    public void play(SoundEffect effect) {
        if (effect == null) {
            return;
        }
        CachedAudio audio = cache.get(effect);
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
            clip.open(audio.format(), audio.pcmData(), 0, audio.pcmData().length); // Abre desde memoria para evitar latencia de disco
            applyMaxVolume(clip);
            clip.start();
        } catch (Exception e) {
            System.err.println("No se pudo reproducir el efecto " + effect + ": " + e.getMessage());
        }
    }

    private void preloadAll() {
        for (SoundEffect effect : SoundEffect.values()) {
            CachedAudio loaded = loadAudio(effect);
            if (loaded != null) {
                cache.put(effect, loaded);
            }
        }
    }

    private CachedAudio loadAudio(SoundEffect effect) {
        try (InputStream rawStream = getClass().getResourceAsStream(effect.getResourcePath())) {
            if (rawStream == null) {
                System.err.println("No se encontró el efecto de sonido: " + effect.getResourcePath());
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
            System.err.println("No se pudo precargar el efecto " + effect + ": " + e.getMessage());
            return null;
        }
    }

    private void applyMaxVolume(Clip clip) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), TARGET_GAIN_DB));
        gain.setValue(clamped);
    }
}
