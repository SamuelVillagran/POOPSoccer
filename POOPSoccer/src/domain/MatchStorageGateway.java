package domain;

import java.io.File;
import java.util.ServiceLoader;

/**
 * Output port (Gateway) decoupling match serialization and save directory resolution from domain logic.
 */
public interface MatchStorageGateway { /*Perfeccionado con Antigravity.*/

    File getSavesDirectory();

    void saveMatch(POOPSoccer match, File file) throws POOPSoccerException;

    POOPSoccer loadMatch(File file) throws POOPSoccerException;

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
