package domain;

import java.util.function.Supplier;

/**
 * Available behavior profiles for machine players. A profile is available when it has a strategy factory.
 */
public enum MachineProfile { /*Perfeccionado con Antigravity.*/
    CLUMSY("Torpe", ClumsyStrategy::new), // Perfil disponible actualmente con movimientos aleatorios
    OFFENSIVE("Ofensiva", null), // Perfil reservado para futura IA enfocada en atacar
    DEFENSIVE("Defensiva", null), // Perfil reservado para futura IA enfocada en proteger el arco
    EXPERT("Experta", null); // Perfil reservado para futura IA táctica adaptativa

    private final String displayName; // Nombre legible mostrado en el selector de perfil CPU
    private final Supplier<MachineStrategy> strategyFactory; // Fábrica del comportamiento; null mientras no esté implementado

    MachineProfile(String displayName, Supplier<MachineStrategy> strategyFactory) {
        this.displayName = displayName;
        this.strategyFactory = strategyFactory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAvailable() {
        return strategyFactory != null;
    }

    public MachineStrategy createStrategy() {
        if (!isAvailable()) {
            throw new POOPSoccerException(POOPSoccerException.UNAVAILABLE_MACHINE_PROFILE + displayName);
        }
        return strategyFactory.get();
    }

    public static MachineProfile fromDisplayName(String name) {
        return EnumResolver.resolve(values(), name, CLUMSY, MachineProfile::getDisplayName);
    }
}
