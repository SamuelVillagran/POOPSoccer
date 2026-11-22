package domain;

import java.io.File;
import java.io.Serializable;
import java.util.ServiceLoader;

/**
 * Output port (Gateway) decoupling match serialization and save directory resolution from domain logic.
 */
public interface MatchStorageGateway { /*Perfeccionado con Antigravity.*/

    File getSavesDirectory();

    void saveMatch(Serializable matchState, File file) throws POOPSoccerException;

    <T extends Serializable> T loadMatch(File file, Class<T> expectedType) throws POOPSoccerException;

    /**
     * Resolves the default MatchStorageGateway implementation discovered via SPI.
     */
    static MatchStorageGateway resolveDefault() {
        try {
            for (ServiceLoader.Provider<MatchStorageGateway> provider
                    : ServiceLoader.load(MatchStorageGateway.class).stream().toList()) {
                try {
                    return provider.get();
                } catch (Exception | LinkageError err) {
                    ErrorLogger.report(err instanceof Exception ex ? ex : new RuntimeException(err));
                }
            }
        } catch (Exception | LinkageError err) {
            ErrorLogger.report(err instanceof Exception ex ? ex : new RuntimeException(err));
        }
        throw new POOPSoccerException(POOPSoccerException.SAVE_ERROR);
    }
}
