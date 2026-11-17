package domain;

import java.io.Serializable;
import java.util.List;

/**
 * Boundary interface (Data Gateway) that decouples domain logic from the database implementation.
 */
public interface PlayerDataGateway extends Serializable {

    /**
     * Saves the player points and stat levels into the persistence store.
     */
    void savePlayerProgress(Player player);

    /**
     * Loads the stored points and stat levels for the given player if a record exists.
     */
    boolean loadPlayerProgress(Player player);

    /**
     * Returns the most recently used human player names stored in the database.
     */
    default List<String> getRecentPlayerNames(int limit) {
        return List.of();
    }
}
