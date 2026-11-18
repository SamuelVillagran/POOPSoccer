package domain;

/**
 * Represents the upgradable statistics of a player in the domain.
 */
public enum PlayerStat {
    SPEED("speed", "Vel"), // Controla la rapidez de desplazamiento horizontal del personaje
    JUMP("jump", "Salto"), // Controla la altura que alcanza al saltar
    KICK("kick", "Patada"), // Aumenta el daño infligido al rival en patadas y tiros especiales
    SURVIVAL("survival", "Vida"); // Aumenta la vida máxima antes de caer aturdido

    private final String key; // Identificador técnico usado internamente y en pruebas
    private final String displayName; // Etiqueta corta en español mostrada al jugador en la interfaz

    PlayerStat(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a PlayerStat by its internal key or its UI display name.
     */
    public static PlayerStat fromName(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        for (PlayerStat stat : values()) { // Empareja por clave interna ("speed") o etiqueta visible ("Vel")
            if (stat.key.equalsIgnoreCase(trimmed) || stat.displayName.equalsIgnoreCase(trimmed)) {
                return stat;
            }
        }
        return null;
    }
}
