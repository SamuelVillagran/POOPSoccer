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

    private static final String DEFAULT_DB_NAME = "players_db.csv"; // Archivo CSV de respaldo cuando no hay SQLite disponible
    private static final String HEADER = "name;points;speed;jump;kick;life"; // Encabezado estándar de columnas separadas por punto y coma
    private static final String LEGACY_HEADER = "name;points;speed;jump;kick;survival"; // Encabezado heredado compatible con archivos previos
    private final File databaseFile; // Ruta del archivo CSV donde persisten los perfiles de respaldo

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
        Map<String, String> records = readAllRecords(); // Carga los registros existentes antes de actualizar el perfil
        String key = normalizeKey(player.getName());
        records.put(key, mapPlayerToRow(player));
        writeAllRecords(records);
    }

    @Override
    public boolean loadPlayerProgress(Player player) {
        if (player == null || player.getName() == null || player.getName().isBlank()) {
            return false;
        }
        Map<String, String> records = readAllRecords();
        String row = records.get(normalizeKey(player.getName())); // Busca al jugador ignorando mayúsculas y espacios extras
        if (row == null) {
            return false;
        }
        return mapRowToPlayer(row, player);
    }

    private String mapPlayerToRow(Player player) {
        String cleanName = player.getName().trim().replace(";", ","); // Evita que un punto y coma corrompa las columnas del CSV
        var profile = player.getProfile();
        return cleanName + ";"
                + profile.getPoints() + ";"
                + profile.getStatLevel("speed") + ";"
                + profile.getStatLevel("jump") + ";"
                + profile.getStatLevel("kick") + ";"
                + profile.getStatLevel("life");
    }

    private boolean mapRowToPlayer(String row, Player player) {
        String[] cols = row.split(";");
        if (cols.length < 6) {
            return false;
        }
        try {
            int points = Integer.parseInt(cols[1].trim());
            int speed = Integer.parseInt(cols[2].trim());
            int jump = Integer.parseInt(cols[3].trim());
            int kick = Integer.parseInt(cols[4].trim());
            int life = Integer.parseInt(cols[5].trim());
            player.getProfile().loadProgress(points, speed, jump, kick, life); // Restaura puntos y niveles leídos desde disco
            player.restoreFullHealth();
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
        try (BufferedReader reader = new BufferedReader(new FileReader(databaseFile))) { // Recorre cada línea omitiendo el encabezado
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.equalsIgnoreCase(HEADER) || trimmed.equalsIgnoreCase(LEGACY_HEADER)) {
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
        try {
            File parent = databaseFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // Crea la carpeta database si todavía no existe
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
