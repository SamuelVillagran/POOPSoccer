package domain;

import java.util.Objects;

/**
 * Immutable snapshot of a player's progression profile across domain and persistence boundaries.
 */
public final class PlayerProfileSnapshot {

    private final String name;
    private final int points;
    private final int speedLevel;
    private final int jumpLevel;
    private final int kickLevel;
    private final int lifeLevel;
    private final int matchesPlayed;
    private final int wins;
    private final int goalsFor;
    private final int goalsAgainst;

    public PlayerProfileSnapshot(
            String name,
            int points,
            int speedLevel,
            int jumpLevel,
            int kickLevel,
            int lifeLevel,
            int matchesPlayed,
            int wins,
            int goalsFor,
            int goalsAgainst) {
        this.name = name;
        this.points = points;
        this.speedLevel = speedLevel;
        this.jumpLevel = jumpLevel;
        this.kickLevel = kickLevel;
        this.lifeLevel = lifeLevel;
        this.matchesPlayed = matchesPlayed;
        this.wins = wins;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
    }

    /**
     * Creates a snapshot with zeroed career match statistics.
     */
    public PlayerProfileSnapshot(
            String name,
            int points,
            int speedLevel,
            int jumpLevel,
            int kickLevel,
            int lifeLevel) {
        this(name, points, speedLevel, jumpLevel, kickLevel, lifeLevel, 0, 0, 0, 0);
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public int getJumpLevel() {
        return jumpLevel;
    }

    public int getKickLevel() {
        return kickLevel;
    }

    public int getLifeLevel() {
        return lifeLevel;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlayerProfileSnapshot other)) {
            return false;
        }
        return Objects.equals(name, other.name)
                && points == other.points
                && speedLevel == other.speedLevel
                && jumpLevel == other.jumpLevel
                && kickLevel == other.kickLevel
                && lifeLevel == other.lifeLevel
                && matchesPlayed == other.matchesPlayed
                && wins == other.wins
                && goalsFor == other.goalsFor
                && goalsAgainst == other.goalsAgainst;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                points,
                speedLevel,
                jumpLevel,
                kickLevel,
                lifeLevel,
                matchesPlayed,
                wins,
                goalsFor,
                goalsAgainst
        );
    }

    @Override
    public String toString() {
        return "PlayerProfileSnapshot[name=" + name
                + ", points=" + points
                + ", speedLevel=" + speedLevel
                + ", jumpLevel=" + jumpLevel
                + ", kickLevel=" + kickLevel
                + ", lifeLevel=" + lifeLevel
                + ", matchesPlayed=" + matchesPlayed
                + ", wins=" + wins
                + ", goalsFor=" + goalsFor
                + ", goalsAgainst=" + goalsAgainst + "]";
    }
}
