package domain;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class that records unexpected errors into a human-readable text log file.
 */
public class Log {
    private static final String LOG_FILE_NAME = "POOPSoccer_log.txt"; // Nombre del archivo de registro de errores para el desarrollador
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Formato de fecha legible

    /**
     * Resolves the shared data directory at the repository root alongside Docs/.
     */
    public static File getDataDirectory() {
        return new File("../Docs").isDirectory() ? new File("../data") : new File("data"); // Ubica data/ junto a Docs/ al correr dentro del módulo
    }

    /**
     * Resolves a named subfolder inside the shared data directory.
     */
    public static File getDataSubdirectory(String subfolder) {
        return new File(getDataDirectory(), subfolder);
    }

    /**
     * Records an exception into data/logs/POOPSoccer_log.txt in plain readable text.
     */
    public static void record(Exception e) {
        if (e == null) return;
        try {
            File folder = getDataSubdirectory("logs"); // Garantiza que exista la subcarpeta logs dentro de data/
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File logFile = new File(folder, LOG_FILE_NAME);
            try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) { // Agrega el error en texto plano legible con fecha y traza
                out.println("============================================================");
                out.println("Fecha y hora : " + LocalDateTime.now().format(DATE_FORMAT));
                out.println("Excepción    : " + e.getClass().getName());
                out.println("Mensaje      : " + (e.getMessage() != null ? e.getMessage() : "Sin mensaje"));
                out.println("Traza (Stack Trace):");
                e.printStackTrace(out);
                out.println("============================================================");
                out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
