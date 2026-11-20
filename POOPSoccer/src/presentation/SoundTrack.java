package presentation;

/**
 * Enumeration of game audio tracks and their corresponding classpath resource locations.
 */
public enum SoundTrack { /*Perfeccionado con Antigravity.*/
    MAIN_MENU("/sounds/inMainMenu.wav"), // Música ambiental reproducida en bucle en la pantalla principal
    PLAYER_CONFIG("/sounds/inPlayerConfig.wav"), // Música reproducida mientras se configuran los jugadores
    IN_GAME("/sounds/inGame.wav"), // Pista principal de estadio durante el desarrollo del partido
    WIN("/sounds/win.wav"); // Fanfarria reproducida al finalizar un partido con ganador

    private final String resourcePath; // Ruta relativa dentro de resources/ para abrir la pista WAV

    SoundTrack(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
