package domain;

import java.util.ServiceLoader;

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
        try {
            for (ServiceLoader.Provider<ErrorLogger> provider : ServiceLoader.load(ErrorLogger.class).stream().toList()) {
                try {
                    provider.get().log(e);
                    return;
                } catch (Exception | LinkageError ignored) {
                    // Continúa con el siguiente proveedor o respaldo en consola si falla uno
                }
            }
        } catch (Exception | LinkageError ignored) {
            // Respaldo seguro si falla la carga de servicios SPI
        }
        e.printStackTrace();
    }
}
