package domain;

/**
 * Available behavior profiles for machine players.
 */
public enum MachineProfile {
    CLUMSY("Torpe", true), // Perfil disponible actualmente con movimientos aleatorios
    OFFENSIVE("Ofensiva", false), // Perfil reservado para futura IA enfocada en atacar
    DEFENSIVE("Defensiva", false), // Perfil reservado para futura IA enfocada en proteger el arco
    EXPERT("Experta", false); // Perfil reservado para futura IA táctica adaptativa

    private final String displayName; // Nombre legible mostrado en el selector de perfil CPU
    private final boolean available; // Indica si el comportamiento ya se encuentra implementado

    MachineProfile(String displayName, boolean available) {
        this.displayName = displayName;
        this.available = available;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAvailable() {
        return available;
    }

    public static MachineProfile fromDisplayName(String name) {
        if (name == null) return CLUMSY;
        for (MachineProfile profile : values()) { // Traduce texto visible o nombre de enum al perfil correspondiente
            if (profile.displayName.equalsIgnoreCase(name.trim())
                    || profile.name().equalsIgnoreCase(name.trim())) {
                return profile;
            }
        }
        return CLUMSY;
    }
}
