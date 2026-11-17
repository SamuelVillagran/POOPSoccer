package domain;

/**
 * Available behavior profiles for machine players.
 */
public enum MachineProfile {
    CLUMSY("Torpe", true),
    OFFENSIVE("Ofensiva", false),
    DEFENSIVE("Defensiva", false),
    EXPERT("Experta", false);

    private final String displayName;
    private final boolean available;

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
        // Compara el nombre ingresado con el nombre visible o el identificador del perfil
        for (MachineProfile profile : values()) {
            if (profile.displayName.equalsIgnoreCase(name.trim())
                    || profile.name().equalsIgnoreCase(name.trim())) {
                return profile;
            }
        }
        return CLUMSY;
    }
}
