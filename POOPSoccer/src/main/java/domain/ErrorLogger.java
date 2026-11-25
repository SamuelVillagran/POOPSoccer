package domain;

import java.util.Optional;

/**
 * Output port for reporting unexpected domain errors without depending on persistence or filesystem details.
 */
@FunctionalInterface
public interface ErrorLogger { /*Perfeccionado con Antigravity.*/

    void log(Exception e);

    /**
     * Reports an exception through the registered ErrorLogger provider, falling back to stderr.
     */
    static void report(Exception e) {
        if (e == null) {
            return;
        }
        // Pasa null en onFailure para evitar recursión infinita si falla la carga del propio ErrorLogger vía SPI
        Optional<ErrorLogger> logger = ServiceResolver.loadFirst(ErrorLogger.class, null);
        if (logger.isPresent()) {
            try {
                logger.get().log(e); // Delega el registro del error al proveedor descubierto
                return;
            } catch (Exception | LinkageError ignored) {
                // Recurre a stderr si el proveedor resuelto falla en tiempo de ejecución
            }
        }
        e.printStackTrace(); // Respaldo final en consola cuando no existe ningún ErrorLogger operativo
    }
}
