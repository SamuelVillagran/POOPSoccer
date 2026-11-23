package presentation;

import java.util.List;
import java.util.Objects;

/**
 * Immutable presentation descriptor for a match rule and its optional numeric parameter choices.
 */
public final class MatchTypeOption {

    private final String displayName;
    private final String parameterLabel;
    private final List<Integer> parameterOptions;

    public MatchTypeOption(String displayName, String parameterLabel, List<Integer> parameterOptions) {
        this.displayName = displayName;
        this.parameterLabel = (parameterLabel == null) ? "" : parameterLabel;
        this.parameterOptions = (parameterOptions == null) ? List.of() : List.copyOf(parameterOptions);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getParameterLabel() {
        return parameterLabel;
    }

    public List<Integer> getParameterOptions() {
        return parameterOptions;
    }

    public boolean hasParameter() {
        return !parameterOptions.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MatchTypeOption other)) {
            return false;
        }
        return Objects.equals(displayName, other.displayName)
                && Objects.equals(parameterLabel, other.parameterLabel)
                && Objects.equals(parameterOptions, other.parameterOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, parameterLabel, parameterOptions);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
