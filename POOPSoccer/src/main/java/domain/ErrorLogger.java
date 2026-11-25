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
        Optional<ErrorLogger> logger = ServiceResolver.loadFirst(ErrorLogger.class, null);
        if (logger.isPresent()) {
            try {
                logger.get().log(e);
                return;
            } catch (Exception | LinkageError ignored) {
                // Fall back to stderr if the resolved logger fails at runtime
            }
        }
        e.printStackTrace();
    }
}
