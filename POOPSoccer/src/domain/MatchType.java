package domain;

import java.util.function.BiFunction;

/**
 * Match types for DOPOSoccer.
 */
public enum MatchType {
    PER_GOALS("Por goles", (goalLimit, timeLimitMinutes) -> new GoalMatch(goalLimit)), // Finaliza cuando uno de los jugadores alcanza el límite de goles
    PER_TIME("Por tiempo", (goalLimit, timeLimitMinutes) -> new TimedMatch(timeLimitMinutes)); // Finaliza al vencer el reloj o tras muerte súbita si hay empate

    private final String displayName; // Etiqueta visible en el selector de configuración del partido
    private final BiFunction<Integer, Integer, MatchRule> ruleFactory;

    MatchType(String displayName, BiFunction<Integer, Integer, MatchRule> ruleFactory) {
        this.displayName = displayName;
        this.ruleFactory = ruleFactory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MatchRule createMatchRule(int goalLimit, int timeLimitMinutes) {
        return ruleFactory.apply(goalLimit, timeLimitMinutes);
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
