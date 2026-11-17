package domain;

/**
 * Game modes supported by POOPSoccer.
 */
public enum GameMode {
    PVP,
    PVM,
    MVM;

    public static GameMode fromName(String name) {
        if (name == null) return PVM;
        // Convierte el texto de la modalidad a su valor correspondiente en el enum
        for (GameMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name.trim())) {
                return mode;
            }
        }
        return PVM;
    }
}
