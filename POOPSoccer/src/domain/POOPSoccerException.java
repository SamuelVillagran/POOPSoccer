package domain;

/**
 * Custom exception for domain errors in POOPSoccer.
 */
public class POOPSoccerException extends RuntimeException {
    public static final String INVALID_FILE = "El archivo seleccionado no es válido."; // Error cuando el archivo no existe o es nulo
    public static final String SAVE_ERROR = "No se pudo guardar la partida en el archivo."; // Error de escritura al serializar
    public static final String OPEN_ERROR = "No se pudo abrir la partida desde el archivo."; // Error de lectura o formato incompatible
    public static final String NULL_PLAYER = "El jugador requerido no puede ser nulo."; // Error cuando falta una referencia obligatoria a Player
    public static final String INVALID_PLAYER_NAME = "El jugador debe tener un nombre válido."; // Error al intentar persistir un perfil sin nombre
    public static final String PLAYER_NOT_ASSIGNED = "La estrategia de control no tiene un jugador asignado."; // Error al ejecutar Type antes de asignarle Player
    public static final String NO_DATABASE_FILE = "No hay archivo de base de datos configurado."; // Error cuando falta la ruta de almacenamiento en disco
    public static final String NULL_ARGUMENT = "Un argumento obligatorio no puede ser nulo: "; // Prefijo para parámetros obligatorios nulos

    public POOPSoccerException(String message) {
        super(message);
    }
}
