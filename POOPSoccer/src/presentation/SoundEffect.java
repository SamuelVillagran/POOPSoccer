package presentation;

import java.util.Locale;
import java.util.Optional;

/**
 * Enumeration of short sound effects and their classpath resource locations.
 */
public enum SoundEffect { /*Perfeccionado con Antigravity.*/
    KICK("/sounds/kick.wav"), // Efecto al ejecutar una patada al aire o hacia el rival
    KICK_BALL("/sounds/kickBall.wav"); // Efecto seco de impacto cuando el pie golpea el balón

    private final String resourcePath; // Ruta relativa dentro de resources/ para cargar el archivo WAV

    SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public static Optional<SoundEffect> fromKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        String normalized = key.trim().toUpperCase(Locale.ROOT);
        for (SoundEffect effect : values()) {
            if (effect.name().equals(normalized)) {
                return Optional.of(effect);
            }
        }
        return Optional.empty();
    }
}
