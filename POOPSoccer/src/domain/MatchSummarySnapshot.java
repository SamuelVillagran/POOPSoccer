package domain;

/**
 * Immutable snapshot of the final outcome of a finished match.
 */
public record MatchSummarySnapshot(
        boolean isDraw,
        int goalsP1,
        int goalsP2,
        String winnerName,
        int winnerPointsAwarded,
        int winnerTotalPoints,
        String loserName,
        int loserPointsAwarded,
        int loserTotalPoints
) { /*Perfeccionado con Antigravity.*/
}
