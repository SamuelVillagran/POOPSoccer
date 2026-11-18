package domain;

/**
 * Custom exception for domain errors in POOPSoccer.
 */
public class POOPSoccerException extends Exception {
    public static final String INVALID_FILE = "El archivo seleccionado no es válido."; // Error cuando el archivo no existe o es nulo
    public static final String SAVE_ERROR = "No se pudo guardar la partida en el archivo."; // Error de escritura al serializar
    public static final String OPEN_ERROR = "No se pudo abrir la partida desde el archivo."; // Error de lectura o formato incompatible

    public POOPSoccerException(String message) {
        super(message);
    }
}
