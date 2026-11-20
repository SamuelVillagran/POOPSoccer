package domain;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * Persistent player progression profile holding points, stat levels, and career match history.
 */
public class PlayerProfile implements Serializable { // Responsabilidad de progreso y estadísticas extraída de Player para perfeccionar el principio S
    /*Perfeccionado con Antigravity.*/

    private static final int MAX_STAT = 10; // Nivel máximo alcanzable por cada estadística
    private static final int BASE_UPGRADE_COST = 500; // Costo en puntos de la primera mejora (nivel 1 a 2)
    private static final float BASE_LIFE = 100.0f; // Vida máxima base en el nivel inicial de resistencia
    private static final float STAT_GROWTH_PER_LEVEL = 0.10f; // Incremento del 10% por cada nivel sobre el inicial

    private final EnumMap<PlayerStat, Integer> statLevels = new EnumMap<>(PlayerStat.class); // Nivel actual de cada estadística (1 a 10)
    private int points = 0; // Puntos acumulados del perfil para comprar mejoras
    private int matchesPlayed = 0; // Total histórico de partidos jugados
    private int wins = 0; // Total histórico de partidos ganados
    private int goalsFor = 0; // Goles a favor acumulados en el perfil
    private int goalsAgainst = 0; // Goles en contra acumulados en el perfil

    public PlayerProfile() {
        for (PlayerStat stat : PlayerStat.values()) {
            statLevels.put(stat, 1); // Todas las estadísticas inician en nivel 1
        }
    }

    private static int clampLevel(int level) {
        return Math.max(1, Math.min(MAX_STAT, level));
    }

    public static int getMaxStatLevel() {
        return MAX_STAT;
    }

    public float getStatBonusMultiplier(PlayerStat stat) {
        return 1.0f + STAT_GROWTH_PER_LEVEL * (getStatLevel(stat) - 1); // Suma 10% sobre el valor base por cada nivel extra
    }

    public float getMaxLife() {
        return BASE_LIFE * getStatBonusMultiplier(PlayerStat.LIFE); // Parte en 100 HP y escala según el nivel de vida
    }

    public int getStatLevel(PlayerStat stat) {
        if (stat == null) return 1;
        return statLevels.getOrDefault(stat, 1);
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
        statLevels.merge(stat, 1, Integer::sum);
    }

    public void copyFrom(PlayerProfile previous) {
        if (previous == null) return;
        this.points = Math.max(0, previous.points);
        for (PlayerStat stat : PlayerStat.values()) {
            statLevels.put(stat, clampLevel(previous.getStatLevel(stat)));
        }
        this.matchesPlayed = Math.max(0, previous.matchesPlayed);
        this.wins = Math.max(0, previous.wins);
        this.goalsFor = Math.max(0, previous.goalsFor);
        this.goalsAgainst = Math.max(0, previous.goalsAgainst);
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
        statLevels.put(PlayerStat.SPEED, clampLevel(speed));
        statLevels.put(PlayerStat.JUMP, clampLevel(jump));
        statLevels.put(PlayerStat.KICK, clampLevel(kick));
        statLevels.put(PlayerStat.LIFE, clampLevel(life));
        this.matchesPlayed = Math.max(0, matchesPlayed);
        this.wins = Math.max(0, wins);
        this.goalsFor = Math.max(0, goalsFor);
        this.goalsAgainst = Math.max(0, goalsAgainst);
    }

    /**
     * Resets this profile to initial level 1 values and zero points.
     */
    public void reset() {
        this.points = 0;
        for (PlayerStat stat : PlayerStat.values()) {
            statLevels.put(stat, 1);
        }
        this.matchesPlayed = 0;
        this.wins = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
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
