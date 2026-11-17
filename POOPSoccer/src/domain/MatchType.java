package domain;

/**
 * Match types for DOPOSoccer.
 */
public enum MatchType {
    PER_GOALS("Por goles"),
    PER_TIME("Por tiempo");

    private final String displayName;

    MatchType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MatchType fromDisplayName(String name) {
        if (name == null) return PER_GOALS;
        // Busca el tipo de partido que coincida con el texto mostrado o el nombre del enum
        for (MatchType type : values()) {
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
