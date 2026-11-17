package domain;

/**
 * Represents the upgradable statistics of a player in the domain.
 */
public enum PlayerStat {
    SPEED("speed", "Vel"),
    JUMP("jump", "Salto"),
    KICK("kick", "Patada"),
    SURVIVAL("survival", "Vida");

    private final String key;
    private final String displayName;

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
        // Busca la estadística coincidiendo por clave interna o por etiqueta de visualización
        for (PlayerStat stat : values()) {
            if (stat.key.equalsIgnoreCase(trimmed) || stat.displayName.equalsIgnoreCase(trimmed)) {
                return stat;
            }
        }
        return null;
    }
}
