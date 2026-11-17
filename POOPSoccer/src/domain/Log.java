package domain;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility class that records unexpected errors into a log file inside the logs folder.
 */
public class Log {
    public static final String LOG_FOLDER = "logs";
    public static final String LOG_NAME = "POOPSoccer";

    /**
     * Records an exception into logs/POOPSoccer.log with date, time, and stack trace.
     */
    public static void record(Exception e) {
        try {
            // Verifica que exista la carpeta "logs" al mismo nivel que "src"
            File folder = new File(LOG_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Obtiene el logger del juego y desactiva la salida por consola repetida
            Logger logger = Logger.getLogger(LOG_NAME);
            logger.setUseParentHandlers(false);

            // Abre el archivo dentro de la carpeta "logs" en modo append (true) para conservar registros previos
            String filePath = LOG_FOLDER + File.separator + LOG_NAME + ".log";
            FileHandler file = new FileHandler(filePath, true);

            // Aplica un formato de texto simple y legible con fecha, hora y detalle del error
            file.setFormatter(new SimpleFormatter());
            logger.addHandler(file);

            // Escribe el error grave junto con toda su traza y cierra el archivo
            logger.log(Level.SEVERE, e.toString(), e);
            file.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
