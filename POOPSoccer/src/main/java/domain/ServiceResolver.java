package domain;

import java.util.List;
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
        for (ServiceLoader.Provider<T> provider : discoverProviders(serviceType, onFailure)) {
            Optional<T> resolved = instantiate(provider, onFailure);
            if (resolved.isPresent()) {
                return resolved;
            }
        }
        return Optional.empty();
    }

    // Descubre la lista de proveedores SPI registrados; si falla el descubrimiento devuelve una lista vacía
    private static <T> List<ServiceLoader.Provider<T>> discoverProviders(
            Class<T> serviceType, Consumer<Throwable> onFailure) {
        try {
            return ServiceLoader.load(serviceType).stream().toList();
        } catch (Exception | LinkageError err) {
            notifyFailure(onFailure, err);
            return List.of();
        }
    }

    // Instancia un proveedor individual; si falla permite que el bucle intente con el siguiente proveedor registrado
    private static <T> Optional<T> instantiate(
            ServiceLoader.Provider<T> provider, Consumer<Throwable> onFailure) {
        try {
            return Optional.of(provider.get());
        } catch (Exception | LinkageError err) {
            notifyFailure(onFailure, err);
            return Optional.empty();
        }
    }

    // Ejecuta la acción de manejo de errores con el fallo capturado; se omite si onFailure es null (como en ErrorLogger)
    private static void notifyFailure(Consumer<Throwable> onFailure, Throwable err) {
        if (onFailure != null) {
            onFailure.accept(err);
        }
    }

    // Convierte Throwable a Exception si corresponde y delega el registro al puerto ErrorLogger
    private static void reportError(Throwable err) {
        ErrorLogger.report(err instanceof Exception ex ? ex : new RuntimeException(err));
    }
}
