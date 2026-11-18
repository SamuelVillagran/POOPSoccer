package domain;

/**
 * Match types for DOPOSoccer.
 */
public enum MatchType {
    PER_GOALS("Por goles"), // Finaliza cuando uno de los jugadores alcanza el límite de goles
    PER_TIME("Por tiempo"); // Finaliza al vencer el reloj o tras muerte súbita si hay empate

    private final String displayName; // Etiqueta visible en el selector de configuración del partido

    MatchType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MatchType fromDisplayName(String name) {
        if (name == null) return PER_GOALS;
        for (MatchType type : values()) { // Convierte el texto de presentación al enum correspondiente
            if (type.displayName.equalsIgnoreCase(name.trim())
                    || type.name().equalsIgnoreCase(name.trim())) {
                return type;
            }
        }
        return PER_GOALS;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
