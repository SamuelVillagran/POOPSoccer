package domain;

import java.util.function.BiFunction;
import java.util.function.IntFunction;

/**
 * Match types for DOPOSoccer.
 */
public enum MatchType { /*Perfeccionado con Antigravity.*/
    PER_GOALS("Por goles", "Goles para ganar:", new int[] { 3, 5, 7 }, GoalMatch::new,
            (goalLimit, timeLimitMinutes) -> new GoalMatch(goalLimit)),
    PER_TIME("Por tiempo", "Minutos del partido:", new int[] { 1, 2, 3, 5 }, TimedMatch::new,
            (goalLimit, timeLimitMinutes) -> new TimedMatch(timeLimitMinutes));

    private final String displayName; // Etiqueta visible en el selector de configuración del partido
    private final String parameterLabel;
    private final int[] parameterOptions;
    private final IntFunction<MatchRule> singleParamFactory;
    private final BiFunction<Integer, Integer, MatchRule> ruleFactory; // Interfaz funcional: recibe (goalLimit, timeLimitMinutes) y devuelve un MatchRule

    MatchType(String displayName, String parameterLabel, int[] parameterOptions,
              IntFunction<MatchRule> singleParamFactory, BiFunction<Integer, Integer, MatchRule> ruleFactory) {
        this.displayName = displayName;
        this.parameterLabel = parameterLabel;
        this.parameterOptions = parameterOptions != null ? parameterOptions.clone() : new int[0];
        this.singleParamFactory = singleParamFactory;
        this.ruleFactory = ruleFactory; // Guarda la función fábrica sin instanciar la regla todavía
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getParameterLabel() {
        return parameterLabel;
    }

    public int[] getParameterOptions() {
        return parameterOptions.clone();
    }

    public MatchRule createMatchRule(int parameterValue) {
        return singleParamFactory.apply(parameterValue);
    }

    public MatchRule createMatchRule(int goalLimit, int timeLimitMinutes) {
        return ruleFactory.apply(goalLimit, timeLimitMinutes); // Ejecuta la lambda guardada (apply) y crea una instancia nueva de MatchRule para el partido
    }

    public static MatchType fromDisplayName(String name) {
        return EnumResolver.resolve(values(), name, PER_GOALS, MatchType::getDisplayName); // Convierte el texto de presentación al enum
    }

    @Override
    public String toString() {
        return displayName;
    }
}
