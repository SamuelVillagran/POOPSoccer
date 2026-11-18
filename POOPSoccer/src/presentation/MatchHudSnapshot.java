package presentation;

/**
 * Immutable HUD snapshot containing scoreboard, timer, and both players' status.
 */
public record MatchHudSnapshot(
        PlayerHudInfo player1,
        PlayerHudInfo player2,
        String score,
        float timeRemaining,
        boolean suddenDeath
) {
}
