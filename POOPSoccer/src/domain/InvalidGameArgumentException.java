package domain;

/**
 * Thrown when a game mechanic receives a null collaborator that it cannot operate without.
 */
public class InvalidGameArgumentException extends IllegalArgumentException {

    public InvalidGameArgumentException(String argumentName) {
        super("Required argument '" + argumentName + "' must not be null.");
    }
}
