package domain;

/**
 * Represents the upgradable statistics of a player in the domain.
 */
public enum PlayerStat { /*Perfeccionado con Antigravity.*/
    SPEED("speed", "Vel"), // Controla la rapidez de desplazamiento horizontal del personaje
    JUMP("jump", "Salto"), // Controla la altura que alcanza al saltar
    KICK("kick", "Patada"), // Aumenta el daño infligido al rival en patadas y tiros especiales
    LIFE("life", "Vida"); // Aumenta la vida máxima antes de caer aturdido

    private final String key; // Identificador técnico usado internamente y en pruebas
    private final String displayName; // Etiqueta corta en español mostrada al jugador en la interfaz

    PlayerStat(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a PlayerStat by its internal key or its UI display name.
     */
    public static PlayerStat fromName(String name) {
        if (name == null) return null;
        if ("survival".equalsIgnoreCase(name.trim())) {
            return LIFE; // Acepta "survival" como alias heredado para compatibilidad hacia atrás
        }
        // Empareja por clave interna ("speed", "life") o etiqueta visible ("Vel", "Vida")
        return EnumResolver.resolve(values(), name, null, stat -> stat.key, PlayerStat::getDisplayName);
    }
}
