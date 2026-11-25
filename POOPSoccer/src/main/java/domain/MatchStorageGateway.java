package domain;

import java.io.File;
import java.io.Serializable;

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
        // Busca mediante SPI el adaptador de guardado de partidas; falla explícitamente si no existe ningún proveedor válido
        return ServiceResolver.loadFirst(MatchStorageGateway.class)
                .orElseThrow(() -> new POOPSoccerException(POOPSoccerException.SAVE_ERROR));
    }
}
