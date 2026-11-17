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
    private static final String LOG_FILE_NAME = "POOPSoccer_log.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Resolves the shared data directory at the repository root alongside Docs/.
     */
    public static File getDataDirectory() {
        // Ubica la carpeta data junto a Docs cuando se ejecuta desde la subcarpeta del módulo
        return new File("../Docs").isDirectory() ? new File("../data") : new File("data");
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
            // Garantiza que exista la subcarpeta logs dentro de data/
            File folder = getDataSubdirectory("logs");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File logFile = new File(folder, LOG_FILE_NAME);
            // Escribe el error en texto plano legible con fecha, clase, mensaje y traza completa
            try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
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
