package presentation;

import java.util.Objects;

/**
 * Immutable HUD snapshot for a single player during an active match.
 */
public final class PlayerHudInfo { /*Perfeccionado con Antigravity.*/

    private final String displayName;
    private final String teamName;
    private final String flagKey;
    private final float healthPercent;
    private final float powerPercent;
    private final String activePower;
    private final String statsSummary;

    public PlayerHudInfo(
            String displayName,
            String teamName,
            float healthPercent,
            float powerPercent,
            String activePower,
            String statsSummary) {
        this(displayName, teamName, teamName, healthPercent, powerPercent, activePower, statsSummary);
    }

    public PlayerHudInfo(
            String displayName,
            String teamName,
            String flagKey,
            float healthPercent,
            float powerPercent,
            String activePower,
            String statsSummary) {
        this.displayName = displayName;
        this.teamName = teamName;
        this.flagKey = flagKey;
        this.healthPercent = healthPercent;
        this.powerPercent = powerPercent;
        this.activePower = activePower;
        this.statsSummary = statsSummary;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getFlagKey() {
        return flagKey;
    }

    public float getHealthPercent() {
        return healthPercent;
    }

    public float getPowerPercent() {
        return powerPercent;
    }

    public String getActivePower() {
        return activePower;
    }

    public String getStatsSummary() {
        return statsSummary;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlayerHudInfo other)) {
            return false;
        }
        return Objects.equals(displayName, other.displayName)
                && Objects.equals(teamName, other.teamName)
                && Objects.equals(flagKey, other.flagKey)
                && Float.compare(other.healthPercent, healthPercent) == 0
                && Float.compare(other.powerPercent, powerPercent) == 0
                && Objects.equals(activePower, other.activePower)
                && Objects.equals(statsSummary, other.statsSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, teamName, flagKey, healthPercent, powerPercent, activePower, statsSummary);
    }

    @Override
    public String toString() {
        return "PlayerHudInfo[displayName=" + displayName
                + ", teamName=" + teamName
                + ", flagKey=" + flagKey
                + ", healthPercent=" + healthPercent
                + ", powerPercent=" + powerPercent
                + ", activePower=" + activePower
                + ", statsSummary=" + statsSummary + "]";
    }
}
