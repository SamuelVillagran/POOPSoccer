package domain;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Boundary interface (Data Gateway) that decouples domain logic from the database implementation.
 */
public interface PlayerDataGateway extends Serializable { /*Perfeccionado con Antigravity.*/

    /**
     * Saves the player profile snapshot into the persistence store.
     */
    void savePlayerProfile(PlayerProfileSnapshot profile);

    /**
     * Loads the stored profile snapshot for the given player name if a record exists.
     */
    Optional<PlayerProfileSnapshot> loadPlayerProfile(String playerName);

    /**
     * Returns the most recently used human player names stored in the database.
     */
    default List<String> getRecentPlayerNames(int limit) {
        return List.of();
    }
}
