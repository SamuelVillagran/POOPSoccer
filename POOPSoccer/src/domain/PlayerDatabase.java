package domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Database and Data Mapper implementation that persists player profiles in a structured table file.
 */
public class PlayerDatabase implements PlayerDataGateway {

    private static final String DEFAULT_DB_NAME = "players_db.csv";
    private static final String HEADER = "name;points;speed;jump;kick;survival";
    private final File databaseFile;

    public PlayerDatabase() {
        this(new File(Log.getDataSubdirectory("database"), DEFAULT_DB_NAME));
    }

    public PlayerDatabase(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    @Override
    public void savePlayerProgress(Player player) {
        if (player == null || player.getName() == null || player.getName().isBlank()) {
            return;
        }
        // Lee todos los registros actuales, actualiza la fila del jugador y escribe la tabla en disco
        Map<String, String> records = readAllRecords();
        String key = normalizeKey(player.getName());
        records.put(key, mapPlayerToRow(player));
        writeAllRecords(records);
    }

    @Override
    public boolean loadPlayerProgress(Player player) {
        if (player == null || player.getName() == null || player.getName().isBlank()) {
            return false;
        }
        // Busca la fila del jugador por su nombre normalizado y restaura sus estadísticas
        Map<String, String> records = readAllRecords();
        String row = records.get(normalizeKey(player.getName()));
        if (row == null) {
            return false;
        }
        return mapRowToPlayer(row, player);
    }

    private String mapPlayerToRow(Player player) {
        // Convierte la entidad Player en un registro estructurado separado por punto y coma
        String cleanName = player.getName().trim().replace(";", ",");
        return cleanName + ";"
                + player.getPoints() + ";"
                + player.getStatLevel("speed") + ";"
                + player.getStatLevel("jump") + ";"
                + player.getStatLevel("kick") + ";"
                + player.getStatLevel("survival");
    }

    private boolean mapRowToPlayer(String row, Player player) {
        // Transforma las columnas almacenadas en la base de datos hacia los atributos de la entidad
        String[] cols = row.split(";");
        if (cols.length < 6) {
            return false;
        }
        try {
            int points = Integer.parseInt(cols[1].trim());
            int speed = Integer.parseInt(cols[2].trim());
            int jump = Integer.parseInt(cols[3].trim());
            int kick = Integer.parseInt(cols[4].trim());
            int survival = Integer.parseInt(cols[5].trim());
            player.loadProgress(points, speed, jump, kick, survival);
            return true;
        } catch (NumberFormatException e) {
            Log.record(e);
            return false;
        }
    }

    private Map<String, String> readAllRecords() {
        Map<String, String> records = new LinkedHashMap<>();
        if (databaseFile == null || !databaseFile.exists()) {
            return records;
        }
        // Recorre el archivo de base de datos ignorando el encabezado y carga cada fila por clave
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.equalsIgnoreCase(HEADER)) {
                    continue;
                }
                String[] parts = trimmed.split(";", 2);
                if (parts.length == 2) {
                    records.put(normalizeKey(parts[0]), trimmed);
                }
            }
        } catch (Exception e) {
            Log.record(e);
        }
        return records;
    }

    private void writeAllRecords(Map<String, String> records) {
        if (databaseFile == null) {
            return;
        }
        // Asegura que la carpeta exista y persiste todos los registros bajo el encabezado de la tabla
        try {
            File parent = databaseFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(databaseFile, false))) {
                writer.println(HEADER);
                for (String row : records.values()) {
                    writer.println(row);
                }
            }
        } catch (Exception e) {
            Log.record(e);
        }
    }

    private String normalizeKey(String playerName) {
        return playerName.trim().toLowerCase();
    }
}
