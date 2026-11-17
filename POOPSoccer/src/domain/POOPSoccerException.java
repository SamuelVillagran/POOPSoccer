package domain;

/**
 * Custom exception for domain errors in POOPSoccer.
 */
public class POOPSoccerException extends Exception {
    public static final String INVALID_FILE = "El archivo seleccionado no es válido.";
    public static final String SAVE_ERROR = "No se pudo guardar la partida en el archivo.";
    public static final String OPEN_ERROR = "No se pudo abrir la partida desde el archivo.";

    public POOPSoccerException(String message) {
        super(message);
    }
}
