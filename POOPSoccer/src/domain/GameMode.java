package domain;

/**
 * Game modes supported by POOPSoccer.
 */
public enum GameMode {
    PVP, // Modalidad Jugador contra Jugador en el mismo teclado
    PVM, // Modalidad Jugador contra Máquina
    MVM; // Modalidad Máquina contra Máquina

    public static GameMode fromName(String name) {
        if (name == null) return PVM;
        for (GameMode mode : values()) { // Traduce el identificador de texto al valor correspondiente del enum
            if (mode.name().equalsIgnoreCase(name.trim())) {
                return mode;
            }
        }
        return PVM;
    }
}
