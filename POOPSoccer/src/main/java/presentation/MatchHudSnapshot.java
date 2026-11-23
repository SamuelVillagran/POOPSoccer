package presentation;

import java.util.Objects;

/**
 * Immutable HUD snapshot containing scoreboard, timer, and both players' status.
 */
public final class MatchHudSnapshot { /*Perfeccionado con Antigravity.*/

    private final PlayerHudInfo player1;
    private final PlayerHudInfo player2;
    private final String score;
    private final float timeRemaining;
    private final boolean suddenDeath;

    public MatchHudSnapshot(
            PlayerHudInfo player1,
            PlayerHudInfo player2,
            String score,
            float timeRemaining,
            boolean suddenDeath) {
        this.player1 = player1;
        this.player2 = player2;
        this.score = score;
        this.timeRemaining = timeRemaining;
        this.suddenDeath = suddenDeath;
    }

    public PlayerHudInfo getPlayer1() {
        return player1;
    }

    public PlayerHudInfo getPlayer2() {
        return player2;
    }

    public String getScore() {
        return score;
    }

    public float getTimeRemaining() {
        return timeRemaining;
    }

    public boolean isSuddenDeath() {
        return suddenDeath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MatchHudSnapshot other)) {
            return false;
        }
        return Objects.equals(player1, other.player1)
                && Objects.equals(player2, other.player2)
                && Objects.equals(score, other.score)
                && Float.compare(other.timeRemaining, timeRemaining) == 0
                && suddenDeath == other.suddenDeath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(player1, player2, score, timeRemaining, suddenDeath);
    }

    @Override
    public String toString() {
        return "MatchHudSnapshot[player1=" + player1
                + ", player2=" + player2
                + ", score=" + score
                + ", timeRemaining=" + timeRemaining
                + ", suddenDeath=" + suddenDeath + "]";
    }
}
