package domain;

import java.util.function.Supplier;

/**
 * Game modes supported by POOPSoccer.
 */
public enum GameMode {
    PVP(domain.PVP::new), // Modalidad Jugador contra Jugador en el mismo teclado
    PVM(domain.PVM::new), // Modalidad Jugador contra Máquina
    MVM(domain.MVM::new); // Modalidad Máquina contra Máquina

    private final Supplier<TypeMatch> factory;

    GameMode(Supplier<TypeMatch> factory) {
        this.factory = factory;
    }

    public TypeMatch createTypeMatch() {
        return factory.get();
    }

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
