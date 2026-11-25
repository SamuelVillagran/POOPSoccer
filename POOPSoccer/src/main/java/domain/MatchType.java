package domain;

import java.util.function.IntFunction;

/**
 * Match types for DOPOSoccer.
 */
public enum MatchType { /*Perfeccionado con Antigravity.*/
    PER_GOALS("Por goles", "Goles para ganar:", new int[] { 3, 5, 7 }, GoalMatch::new),
    PER_TIME("Por tiempo", "Minutos del partido:", new int[] { 1, 2, 3, 5 }, TimedMatch::new);

    private final String displayName; // Etiqueta visible en el selector de configuración del partido
    private final String parameterLabel;
    private final int[] parameterOptions;
    private final IntFunction<MatchRule> singleParamFactory;

    MatchType(String displayName, String parameterLabel, int[] parameterOptions,
              IntFunction<MatchRule> singleParamFactory) {
        this.displayName = displayName;
        this.parameterLabel = parameterLabel;
        this.parameterOptions = parameterOptions != null ? parameterOptions.clone() : new int[0];
        this.singleParamFactory = singleParamFactory;
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

    public static MatchType fromDisplayName(String name) {
        return EnumResolver.resolve(values(), name, PER_GOALS, MatchType::getDisplayName); // Convierte el texto de presentación al enum
    }

    @Override
    public String toString() {
        return displayName;
    }
}
