package presentation;

/**
 * Enumeration of short sound effects and their classpath resource locations.
 */
public enum SoundEffect {
    KICK("/sounds/kick.wav"), // Efecto al ejecutar una patada al aire o hacia el rival
    KICK_BALL("/sounds/kickBall.wav"); // Efecto seco de impacto cuando el pie golpea el balón

    private final String resourcePath; // Ruta relativa dentro de resources/ para cargar el archivo WAV

    SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
