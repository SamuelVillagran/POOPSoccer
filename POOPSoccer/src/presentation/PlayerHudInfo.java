package presentation;

/**
 * Immutable HUD snapshot for a single player during an active match.
 */
public record PlayerHudInfo(
        String displayName,
        String teamName,
        float healthPercent,
        float powerPercent,
        String activePower,
        String statsSummary
) {
}
