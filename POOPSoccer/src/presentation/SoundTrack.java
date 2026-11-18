package presentation;

/**
 * Enumeration of game audio tracks and their corresponding classpath resource locations.
 */
public enum SoundTrack {
    MAIN_MENU("/sounds/inMainMenu.wav"),
    PLAYER_CONFIG("/sounds/inPlayerConfig.wav"),
    IN_GAME("/sounds/inGame.wav"),
    WIN("/sounds/win.wav");

    private final String resourcePath;

    SoundTrack(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
