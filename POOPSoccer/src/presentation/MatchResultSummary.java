package presentation;

/**
 * Immutable presentation DTO describing a finished match without UI formatting coupling.
 */
public record MatchResultSummary(
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
