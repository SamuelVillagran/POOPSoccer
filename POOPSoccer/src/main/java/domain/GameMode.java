package domain;

import java.util.function.Supplier;

/**
 * Game modes supported by POOPSoccer.
 */
public enum GameMode { /*Perfeccionado con Antigravity.*/
    PVP("Jugador vs Jugador (PvP)", domain.PVP::new), // Modalidad Jugador contra Jugador en el mismo teclado
    PVM("Jugador vs Máquina (PvM)", domain.PVM::new), // Modalidad Jugador contra Máquina
    MVM("Máquina vs Máquina (MvM)", domain.MVM::new); // Modalidad Máquina contra Máquina

    private final String displayName;
    private final Supplier<TypeMatch> factory;

    GameMode(String displayName, Supplier<TypeMatch> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TypeMatch createTypeMatch() {
        return factory.get();
    }

    public static GameMode fromName(String name) {
        return EnumResolver.resolve(values(), name, PVM); // Traduce el identificador de texto al valor del enum
    }
}
