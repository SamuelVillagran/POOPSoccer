package presentation;

import java.util.Objects;

/**
 * Immutable presentation DTO describing a finished match without UI formatting coupling.
 */
public final class MatchResultSummary { /*Perfeccionado con Antigravity.*/

    private final boolean draw;
    private final int goalsP1;
    private final int goalsP2;
    private final String winnerName;
    private final int winnerPointsAwarded;
    private final int winnerTotalPoints;
    private final String loserName;
    private final int loserPointsAwarded;
    private final int loserTotalPoints;

    public MatchResultSummary(
            boolean isDraw,
            int goalsP1,
            int goalsP2,
            String winnerName,
            int winnerPointsAwarded,
            int winnerTotalPoints,
            String loserName,
            int loserPointsAwarded,
            int loserTotalPoints) {
        this.draw = isDraw;
        this.goalsP1 = goalsP1;
        this.goalsP2 = goalsP2;
        this.winnerName = winnerName;
        this.winnerPointsAwarded = winnerPointsAwarded;
        this.winnerTotalPoints = winnerTotalPoints;
        this.loserName = loserName;
        this.loserPointsAwarded = loserPointsAwarded;
        this.loserTotalPoints = loserTotalPoints;
    }

    public boolean isDraw() {
        return draw;
    }

    public int getGoalsP1() {
        return goalsP1;
    }

    public int getGoalsP2() {
        return goalsP2;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public int getWinnerPointsAwarded() {
        return winnerPointsAwarded;
    }

    public int getWinnerTotalPoints() {
        return winnerTotalPoints;
    }

    public String getLoserName() {
        return loserName;
    }

    public int getLoserPointsAwarded() {
        return loserPointsAwarded;
    }

    public int getLoserTotalPoints() {
        return loserTotalPoints;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MatchResultSummary other)) {
            return false;
        }
        return draw == other.draw
                && goalsP1 == other.goalsP1
                && goalsP2 == other.goalsP2
                && Objects.equals(winnerName, other.winnerName)
                && winnerPointsAwarded == other.winnerPointsAwarded
                && winnerTotalPoints == other.winnerTotalPoints
                && Objects.equals(loserName, other.loserName)
                && loserPointsAwarded == other.loserPointsAwarded
                && loserTotalPoints == other.loserTotalPoints;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                draw,
                goalsP1,
                goalsP2,
                winnerName,
                winnerPointsAwarded,
                winnerTotalPoints,
                loserName,
                loserPointsAwarded,
                loserTotalPoints
        );
    }

    @Override
    public String toString() {
        return "MatchResultSummary[draw=" + draw
                + ", goalsP1=" + goalsP1
                + ", goalsP2=" + goalsP2
                + ", winnerName=" + winnerName
                + ", winnerPointsAwarded=" + winnerPointsAwarded
                + ", winnerTotalPoints=" + winnerTotalPoints
                + ", loserName=" + loserName
                + ", loserPointsAwarded=" + loserPointsAwarded
                + ", loserTotalPoints=" + loserTotalPoints + "]";
    }
}
