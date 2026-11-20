package presentation;

import java.util.List;

/**
 * Immutable presentation descriptor for a match rule and its optional numeric parameter choices.
 */
public record MatchTypeOption(String displayName, String parameterLabel, List<Integer> parameterOptions) {

    public MatchTypeOption {
        parameterOptions = (parameterOptions == null) ? List.of() : List.copyOf(parameterOptions);
        parameterLabel = (parameterLabel == null) ? "" : parameterLabel;
    }

    public boolean hasParameter() {
        return !parameterOptions.isEmpty();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
