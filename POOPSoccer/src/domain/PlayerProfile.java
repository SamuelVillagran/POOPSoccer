package domain;

import java.io.Serializable;

/**
 * Persistent player progression profile holding points, stat levels, and career match history.
 */
public class PlayerProfile implements Serializable { // Responsabilidad de progreso y estadísticas extraída de Player para perfeccionar el principio S

    private static final int MAX_STAT = 10; // Nivel máximo alcanzable por cada estadística
    private static final int BASE_UPGRADE_COST = 500; // Costo en puntos de la primera mejora (nivel 1 a 2)
    private static final float BASE_LIFE = 100.0f; // Vida máxima base en el nivel inicial de resistencia
    private static final float STAT_GROWTH_PER_LEVEL = 0.10f; // Incremento del 10% por cada nivel sobre el inicial

    private int speedStat = 1; // Nivel actual de velocidad (1 a 10)
    private int jumpStat = 1; // Nivel actual de salto (1 a 10)
    private int kickStat = 1; // Nivel actual de fuerza/daño de patada (1 a 10)
    private int lifeStat = 1; // Nivel actual de vida máxima (1 a 10)
    private int points = 0; // Puntos acumulados del perfil para comprar mejoras
    private int matchesPlayed = 0; // Total histórico de partidos jugados
    private int wins = 0; // Total histórico de partidos ganados
    private int goalsFor = 0; // Goles a favor acumulados en el perfil
    private int goalsAgainst = 0; // Goles en contra acumulados en el perfil

    public float getStatBonusMultiplier(PlayerStat stat) {
        return 1.0f + STAT_GROWTH_PER_LEVEL * (getStatLevel(stat) - 1); // Suma 10% sobre el valor base por cada nivel extra
    }

    public float getMaxLife() {
        return BASE_LIFE * getStatBonusMultiplier(PlayerStat.LIFE); // Parte en 100 HP y escala según el nivel de vida
    }

    public int getStatLevel(PlayerStat stat) {
        if (stat == null) return 1;
        return switch (stat) {
            case SPEED -> speedStat;
            case JUMP -> jumpStat;
            case KICK -> kickStat;
            case LIFE -> lifeStat;
        };
    }

    public int getStatLevel(String stat) {
        return getStatLevel(PlayerStat.fromName(stat));
    }

    public int getUpgradeCost(String stat) {
        int level = getStatLevel(stat);
        return BASE_UPGRADE_COST * (int) Math.pow(2, level - 1); // Duplica el precio en cada nivel: 500, 1000, 2000...
    }

    public boolean upgradeStat(String stat) {
        PlayerStat resolved = PlayerStat.fromName(stat);
        if (resolved == null) return false;
        int level = getStatLevel(resolved);
        int cost = getUpgradeCost(stat);
        if (level >= MAX_STAT || points < cost) return false;
        points -= cost;
        incrementStat(resolved);
        return true;
    }

    private void incrementStat(PlayerStat stat) {
        switch (stat) {
            case SPEED -> speedStat++;
            case JUMP -> jumpStat++;
            case KICK -> kickStat++;
            case LIFE -> lifeStat++;
        }
    }

    public void copyFrom(PlayerProfile previous) {
        if (previous == null) return;
        loadProgress(previous.points, previous.speedStat, previous.jumpStat, previous.kickStat,
                previous.lifeStat, previous.matchesPlayed, previous.wins, previous.goalsFor, previous.goalsAgainst);
    }

    /**
     * Loads the stored points and stat levels into this profile.
     */
    public void loadProgress(int points, int speed, int jump, int kick, int life) {
        loadProgress(points, speed, jump, kick, life, 0, 0, 0, 0);
    }

    /**
     * Loads stored points, stat levels, and career match statistics into this profile.
     */
    public void loadProgress(int points, int speed, int jump, int kick, int life,
                             int matchesPlayed, int wins, int goalsFor, int goalsAgainst) {
        this.points = Math.max(0, points); // Garantiza valores no negativos y niveles acotados entre 1 y 10
        this.speedStat = Math.max(1, Math.min(MAX_STAT, speed));
        this.jumpStat = Math.max(1, Math.min(MAX_STAT, jump));
        this.kickStat = Math.max(1, Math.min(MAX_STAT, kick));
        this.lifeStat = Math.max(1, Math.min(MAX_STAT, life));
        this.matchesPlayed = Math.max(0, matchesPlayed);
        this.wins = Math.max(0, wins);
        this.goalsFor = Math.max(0, goalsFor);
        this.goalsAgainst = Math.max(0, goalsAgainst);
    }

    /**
     * Resets this profile to initial level 1 values and zero points.
     */
    public void reset() {
        loadProgress(0, 1, 1, 1, 1, 0, 0, 0, 0);
    }

    /**
     * Records the outcome and goal totals of a finished match into this profile.
     */
    public void recordMatchResult(boolean won, int goalsScored, int goalsConceded) {
        this.matchesPlayed++;
        if (won) this.wins++;
        this.goalsFor += Math.max(0, goalsScored);
        this.goalsAgainst += Math.max(0, goalsConceded);
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        if (amount > 0) points += amount; // Acumula puntos ganados ignorando montos no positivos
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
}
