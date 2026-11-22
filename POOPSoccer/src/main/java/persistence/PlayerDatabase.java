package persistence;

import domain.POOPSoccerException;
import domain.PlayerDataGateway;
import domain.PlayerProfileSnapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Database and Data Mapper implementation that persists player profiles in a structured table file.
 */
public class PlayerDatabase implements PlayerDataGateway { /*Perfeccionado con Antigravity.*/

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
    public void savePlayerProfile(PlayerProfileSnapshot profile) {
        if (profile == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER);
        }
        if (profile.name() == null || profile.name().isBlank()) {
            throw new POOPSoccerException(POOPSoccerException.INVALID_PLAYER_NAME);
        }
        Map<String, String> records = readAllRecords(); // Loads existing records before updating the profile
        String key = normalizeKey(profile.name());
        records.put(key, mapSnapshotToRow(profile));
        writeAllRecords(records);
    }

    @Override
    public Optional<PlayerProfileSnapshot> loadPlayerProfile(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }
        Map<String, String> records = readAllRecords();
        String row = records.get(normalizeKey(playerName)); // Looks up player ignoring case and extra whitespace
        if (row == null) {
            return Optional.empty();
        }
        return mapRowToSnapshot(row);
    }

    private String mapSnapshotToRow(PlayerProfileSnapshot profile) {
        String cleanName = profile.name().trim().replace(";", ","); // Prevents semicolons from corrupting CSV columns
        return cleanName + ";"
                + profile.points() + ";"
                + profile.speedLevel() + ";"
                + profile.jumpLevel() + ";"
                + profile.kickLevel() + ";"
                + profile.lifeLevel();
    }

    private Optional<PlayerProfileSnapshot> mapRowToSnapshot(String row) {
        String[] cols = row.split(";");
        if (cols.length < 6) {
            return Optional.empty();
        }
        try {
            int points = Integer.parseInt(cols[1].trim());
            int speed = Integer.parseInt(cols[2].trim());
            int jump = Integer.parseInt(cols[3].trim());
            int kick = Integer.parseInt(cols[4].trim());
            int life = Integer.parseInt(cols[5].trim());
            return Optional.of(new PlayerProfileSnapshot(cols[0].trim(), points, speed, jump, kick, life));
        } catch (NumberFormatException e) {
            Log.record(e);
            return Optional.empty();
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
            throw new POOPSoccerException(POOPSoccerException.NO_DATABASE_FILE);
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
