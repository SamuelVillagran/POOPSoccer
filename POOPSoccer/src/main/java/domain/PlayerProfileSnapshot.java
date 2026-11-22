package domain;

/**
 * Immutable snapshot of a player's progression profile across domain and persistence boundaries.
 */
public record PlayerProfileSnapshot(
        String name,
        int points,
        int speedLevel,
        int jumpLevel,
        int kickLevel,
        int lifeLevel,
        int matchesPlayed,
        int wins,
        int goalsFor,
        int goalsAgainst
) {
    /**
     * Creates a snapshot with zeroed career match statistics.
     */
    public PlayerProfileSnapshot(
            String name,
            int points,
            int speedLevel,
            int jumpLevel,
            int kickLevel,
            int lifeLevel
    ) {
        this(name, points, speedLevel, jumpLevel, kickLevel, lifeLevel, 0, 0, 0, 0);
    }
}
