package domain;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Shared SPI discovery helper that loads the first working provider for a domain port.
 */
final class ServiceResolver {

    private ServiceResolver() {
    }

    /**
     * Loads the first instantiable SPI provider for the given service interface, reporting failures via ErrorLogger.
     */
    // Sobrecarga que usa ServiceResolver::reportError como acción por defecto ante cualquier fallo de carga
    static <T> Optional<T> loadFirst(Class<T> serviceType) {
        return loadFirst(serviceType, ServiceResolver::reportError);
    }

    /**
     * Loads the first instantiable SPI provider for the given service interface, forwarding failures to onFailure.
     */
    // <T> define un tipo genérico reutilizable; Class<T> indica la interfaz a buscar vía SPI;
    // Optional<T> envuelve el proveedor encontrado o devuelve vacío; Consumer<Throwable> recibe la acción a ejecutar si ocurre un fallo
    static <T> Optional<T> loadFirst(Class<T> serviceType, Consumer<Throwable> onFailure) {
        if (serviceType == null) {
            return Optional.empty();
        }
        try {
            for (ServiceLoader.Provider<T> provider
                    : ServiceLoader.load(serviceType).stream().toList()) {
                try {
                    return Optional.of(provider.get()); // Instancia y devuelve el primer proveedor válido dentro de Optional
                } catch (Exception | LinkageError err) {
                    if (onFailure != null) {
                        onFailure.accept(err); // Ejecuta la acción de manejo de errores pasándole la excepción o error capturado
                    }
                }
            }
        } catch (Exception | LinkageError err) {
            if (onFailure != null) {
                onFailure.accept(err); // Invoca la acción recibida con el fallo general de carga SPI; se omite si es null (como en ErrorLogger)
            }
        }
        return Optional.empty();
    }

    // Convierte Throwable a Exception si corresponde y delega el registro al puerto ErrorLogger
    private static void reportError(Throwable err) {
        ErrorLogger.report(err instanceof Exception ex ? ex : new RuntimeException(err));
    }
}
