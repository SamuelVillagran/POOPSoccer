package domain;

/**
 * Thrown when player data cannot be persisted because the data store is not properly configured.
 */
public class PersistenceException extends IllegalStateException {

    public static final String NO_DATABASE_FILE = "No database file is configured; player data cannot be saved."; // La ruta de persistencia es nula

    public PersistenceException(String message) {
        super(message);
    }
}
