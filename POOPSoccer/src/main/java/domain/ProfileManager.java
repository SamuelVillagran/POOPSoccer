package domain;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Manages player progression, stat upgrades, match rewards, and profile persistence through PlayerDataGateway.
 */
public class ProfileManager implements Serializable { // Responsabilidad de progreso y persistencia de perfiles extraída de la fachada POOPSoccer para perfeccionar el principio S con ayuda de Gemini Neon 4
    /*Perfeccionado con Antigravity.*/

    public static final int WINNER_POINTS_REWARD = 100; // Puntos otorgados al ganador de cada partido
    public static final int LOSER_POINTS_REWARD = 25; // Puntos otorgados al perdedor por participar

    private PlayerDataGateway dataGateway; // Puerto de persistencia desacoplado para guardar y cargar perfiles

    public ProfileManager(PlayerDataGateway dataGateway) {
        this.dataGateway = dataGateway;
    }

    /**
     * Builds a manager wired to the default PlayerDataGateway discovered via SPI.
     */
    public static ProfileManager withDefaultGateway() {
        return new ProfileManager(resolveDefaultDataGateway());
    }

    private static PlayerDataGateway resolveDefaultDataGateway() {
        return ServiceResolver.loadFirst(PlayerDataGateway.class).orElse(null);
    }

    /**
     * Injects the data gateway implementation used to persist and load player profiles.
     */
    public void setDataGateway(PlayerDataGateway dataGateway) {
        this.dataGateway = dataGateway;
    }

    /**
     * Restores the default gateway when deserializing a match whose gateway was null.
     */
    public void ensureGatewayConnected() {
        if (this.dataGateway == null) {
            this.dataGateway = resolveDefaultDataGateway(); // Reconecta el gateway si faltaba en el binario guardado
        }
    }

    /**
     * Resolves initial player names from the most recent stored human profiles, falling back to defaults.
     */
    public String[] resolveDefaultNames(String fallbackP1, String fallbackP2) {
        String[] names = new String[] { fallbackP1, fallbackP2 };
        if (dataGateway != null) {
            List<String> recent = dataGateway.getRecentPlayerNames(2); // Sugiere los últimos perfiles humanos guardados
            if (!recent.isEmpty()) names[0] = recent.get(0);
            if (recent.size() > 1) names[1] = recent.get(1);
        }
        return names;
    }

    /**
     * Loads stored profile progress into the given player when a gateway is present.
     */
    public void loadIfPresent(Player player) {
        if (dataGateway == null || player == null) return;
        loadIntoPlayer(player);
    }

    /**
     * Restores a player's progress from the gateway or falls back to matching in-memory profile history.
     */
    public void restoreProgress(Player current, Player previous) {
        if (current == null) return;
        if (loadIntoPlayer(current)) {
            return;
        }
        if (previous != null && previous.getName() != null
                && previous.getName().trim().equalsIgnoreCase(current.getName().trim())) {
            current.copyProfileFrom(previous); // Conserva el progreso en memoria si coincide el nombre previo
        }
    }

    /**
     * Persists a player's profile through the active gateway when available.
     */
    public void save(Player player) {
        if (dataGateway != null && player != null) {
            dataGateway.savePlayerProfile(player.toProfileSnapshot());
        }
    }

    /**
     * Selects a player profile by name and loads its stored points and stats from the data gateway.
     */
    public void selectProfile(Player player, String playerName) {
        if (player == null || playerName == null || playerName.isBlank()) return;
        player.setName(playerName.trim());
        if (!loadIntoPlayer(player)) {
            player.resetProfile(); // Inicializa nivel 1 y 0 puntos si el nombre es nuevo
        }
    }

    private boolean loadIntoPlayer(Player player) {
        if (dataGateway == null || player == null || player.getName() == null || player.getName().isBlank()) {
            return false;
        }
        Optional<PlayerProfileSnapshot> snapshot = dataGateway.loadPlayerProfile(player.getName());
        if (snapshot.isEmpty()) {
            return false;
        }
        player.applyProfileSnapshot(snapshot.get());
        return true;
    }

    /**
     * Attempts to upgrade the specified stat for the player and persists the updated profile.
     */
    public boolean upgradeStat(Player player, PlayerStat stat) {
        if (player != null && player.upgradeStat(stat)) {
            save(player);
            return true;
        }
        return false;
    }

    /**
     * Credits match points, records match outcomes into both profiles, and persists them.
     */
    public void recordMatchRewards(Player player1, Player player2, Player winner) {
        if (player1 == null || player2 == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER);
        }
        if (winner == player1) {
            player1.addPoints(WINNER_POINTS_REWARD);
            player2.addPoints(LOSER_POINTS_REWARD);
        } else if (winner == player2) {
            player2.addPoints(WINNER_POINTS_REWARD);
            player1.addPoints(LOSER_POINTS_REWARD);
        }
        player1.recordMatchResult(winner == player1, player1.getGoals(), player2.getGoals());
        player2.recordMatchResult(winner == player2, player2.getGoals(), player1.getGoals());
        save(player1);
        save(player2);
    }

    /**
     * Builds an immutable end-of-match summary snapshot from both players' final state.
     */
    public MatchSummarySnapshot buildMatchSummary(Player player1, Player player2, Player winner) {
        int goalsP1 = player1 != null ? player1.getGoals() : 0;
        int goalsP2 = player2 != null ? player2.getGoals() : 0;
        if (winner == null) {
            return new MatchSummarySnapshot(true, goalsP1, goalsP2, null, 0, 0, null, 0, 0);
        }
        Player loser = (winner == player1) ? player2 : player1;
        return new MatchSummarySnapshot(
                false,
                goalsP1,
                goalsP2,
                winner.getName(),
                WINNER_POINTS_REWARD,
                winner.getPoints(),
                loser.getName(),
                LOSER_POINTS_REWARD,
                loser.getPoints()
        );
    }
}
