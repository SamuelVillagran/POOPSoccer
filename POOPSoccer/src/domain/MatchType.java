package domain;

/**
 * Match types for DOPOSoccer.
 */
public enum MatchType {
    PER_GOALS,
    PER_TIME;

    @Override
    public String toString() {
        return switch (this) {
            case PER_GOALS -> "Por goles";
            case PER_TIME  -> "Por tiempo";
        };
    }
}
