package domain;

/**
 * Thrown when an operation receives a missing or unusable player (null or without a valid name).
 */
public class InvalidPlayerException extends IllegalArgumentException {

    public static final String NULL_PLAYER = "A required player is null."; // Un jugador obligatorio no fue entregado
    public static final String INVALID_NAME = "The player must have a non-blank name."; // El perfil no puede persistirse sin nombre

    public InvalidPlayerException(String message) {
        super(message);
    }
}
