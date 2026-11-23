package persistence;

import domain.MatchStorageGateway;
import domain.POOPSoccerException;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;

/**
 * File-system serialization adapter implementing MatchStorageGateway for saving and loading matches (.dat).
 */
public class FileMatchStorage implements MatchStorageGateway { /*Perfeccionado con Antigravity.*/

    @Override
    public File getSavesDirectory() {
        return Log.getDataSubdirectory("saves");
    }

    @Override
    public void saveMatch(Serializable matchState, File file) throws POOPSoccerException {
        if (matchState == null || file == null) {
            throw new POOPSoccerException(POOPSoccerException.INVALID_FILE);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file.toPath()))) { // Serializes the match state
            out.writeObject(matchState);
        } catch (Exception e) {
            Log.record(e);
            throw new POOPSoccerException(POOPSoccerException.SAVE_ERROR);
        }
    }

    @Override
    public <T extends Serializable> T loadMatch(File file, Class<T> expectedType) throws POOPSoccerException {
        if (file == null || !file.exists() || expectedType == null) {
            throw new POOPSoccerException(POOPSoccerException.INVALID_FILE);
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file.toPath()))) { // Deserializes the saved match state
            return expectedType.cast(in.readObject());
        } catch (Exception e) {
            Log.record(e);
            throw new POOPSoccerException(POOPSoccerException.OPEN_ERROR);
        }
    }
}
