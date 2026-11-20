package persistence;

import domain.MatchStorageGateway;
import domain.POOPSoccer;
import domain.POOPSoccerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * File-system serialization adapter implementing MatchStorageGateway for saving and loading matches (.dat).
 */
public class FileMatchStorage implements MatchStorageGateway { /*Perfeccionado con Antigravity.*/

    @Override
    public File getSavesDirectory() {
        return Log.getDataSubdirectory("saves");
    }

    @Override
    public void saveMatch(POOPSoccer match, File file) throws POOPSoccerException {
        if (match == null || file == null) {
            throw new POOPSoccerException(POOPSoccerException.INVALID_FILE);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) { // Serializa la instancia completa del juego
            out.writeObject(match);
        } catch (Exception e) {
            Log.record(e);
            throw new POOPSoccerException(POOPSoccerException.SAVE_ERROR);
        }
    }

    @Override
    public POOPSoccer loadMatch(File file) throws POOPSoccerException {
        if (file == null || !file.exists()) {
            throw new POOPSoccerException(POOPSoccerException.INVALID_FILE);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) { // Deserializa el partido guardado
            return (POOPSoccer) in.readObject();
        } catch (Exception e) {
            Log.record(e);
            throw new POOPSoccerException(POOPSoccerException.OPEN_ERROR);
        }
    }
}
