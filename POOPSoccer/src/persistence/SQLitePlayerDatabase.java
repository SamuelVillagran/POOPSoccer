package persistence;

import domain.POOPSoccerException;
import domain.PlayerDataGateway;
import domain.PlayerProfileSnapshot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite database and data mapper implementation for the PlayerDataGateway boundary.
 */
public class SQLitePlayerDatabase implements PlayerDataGateway {// Ayudado a implementar con Gemini Argon 4
    /*Perfeccionado con Antigravity.*/

    private static final String DEFAULT_DB_NAME = "poopsoccer.db";
    private static final String CREATE_PLAYERS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS players (
                profile_key TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                points INTEGER NOT NULL DEFAULT 0,
                speed_level INTEGER NOT NULL DEFAULT 1,
                jump_level INTEGER NOT NULL DEFAULT 1,
                kick_level INTEGER NOT NULL DEFAULT 1,
                life_level INTEGER NOT NULL DEFAULT 1,
                matches_played INTEGER NOT NULL DEFAULT 0,
                wins INTEGER NOT NULL DEFAULT 0,
                goals_for INTEGER NOT NULL DEFAULT 0,
                goals_against INTEGER NOT NULL DEFAULT 0,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;
    private static final String UPSERT_PLAYER_SQL = """
            INSERT INTO players (
                profile_key, name, points, speed_level, jump_level, kick_level,
                life_level, matches_played, wins, goals_for, goals_against, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(profile_key) DO UPDATE SET
                name = excluded.name,
                points = excluded.points,
                speed_level = excluded.speed_level,
                jump_level = excluded.jump_level,
                kick_level = excluded.kick_level,
                life_level = excluded.life_level,
                matches_played = excluded.matches_played,
                wins = excluded.wins,
                goals_for = excluded.goals_for,
                goals_against = excluded.goals_against,
                updated_at = CURRENT_TIMESTAMP
            """;
    private static final String SELECT_PLAYER_SQL = """
            SELECT name, points, speed_level, jump_level, kick_level, life_level,
                   matches_played, wins, goals_for, goals_against
            FROM players
            WHERE profile_key = ?
            """;
    private static final String SELECT_RECENT_NAMES_SQL = """
            SELECT name
            FROM players
            WHERE profile_key NOT LIKE 'cpu%'
              AND profile_key NOT IN ('máquina', 'maquina', 'máquina 1', 'maquina 1', 'máquina 2', 'maquina 2')
            ORDER BY updated_at DESC, rowid ASC
            LIMIT ?
            """;

    private final String dbPath;
    private transient boolean schemaReady = false;

    public SQLitePlayerDatabase() {
        this(new File(Log.getDataSubdirectory("database"), DEFAULT_DB_NAME));
    }

    public SQLitePlayerDatabase(File databaseFile) {
        this.dbPath = databaseFile != null
                ? databaseFile.getPath()
                : new File(Log.getDataSubdirectory("database"), DEFAULT_DB_NAME).getPath();
    }

    @Override
    public void savePlayerProfile(PlayerProfileSnapshot profile) {
        if (profile == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER);
        }
        if (isInvalidName(profile.name())) {
            throw new POOPSoccerException(POOPSoccerException.INVALID_PLAYER_NAME);
        }
        // Upserts the player row using parameterized SQL to prevent injection
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(UPSERT_PLAYER_SQL)) {
            bindSnapshotToStatement(stmt, profile);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Log.record(e);
        }
    }

    @Override
    public Optional<PlayerProfileSnapshot> loadPlayerProfile(String playerName) {
        if (isInvalidName(playerName)) return Optional.empty();
        // Queries the profile by its normalized key and maps it into an immutable snapshot
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PLAYER_SQL)) {
            stmt.setString(1, normalizeKey(playerName));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSnapshot(rs));
                }
            }
        } catch (SQLException e) {
            Log.record(e);
        }
        return Optional.empty();
    }

    @Override
    public List<String> getRecentPlayerNames(int limit) {
        List<String> names = new ArrayList<>();
        if (limit <= 0) return names;
        // Recupera los nombres de los perfiles humanos más recientes guardados en la base de datos
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_RECENT_NAMES_SQL)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            Log.record(e);
        }
        return names;
    }

    private Connection openConnection() throws SQLException {
        ensureDatabaseDirectory();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        ensureSchema(conn);
        return conn;
    }

    private void ensureDatabaseDirectory() {
        File parent = new File(dbPath).getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private void ensureSchema(Connection conn) throws SQLException {
        if (schemaReady) return;
        // Crea la tabla players si todavía no existe en el archivo de base de datos
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_PLAYERS_TABLE_SQL);
        }
        migrateLegacySurvivalColumn(conn);
        schemaReady = true;
    }

    private void migrateLegacySurvivalColumn(Connection conn) throws SQLException {
        boolean hasSurvival = false;
        boolean hasLife = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(players)")) {
            while (rs.next()) {
                String col = rs.getString("name");
                if ("survival_level".equalsIgnoreCase(col)) hasSurvival = true;
                if ("life_level".equalsIgnoreCase(col)) hasLife = true;
            }
        }
        if (hasSurvival && !hasLife) {
            // Preserva datos previos al renombrar survival_level hacia life_level en bases existentes
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE players RENAME COLUMN survival_level TO life_level");
            }
        }
    }

    private void bindSnapshotToStatement(PreparedStatement stmt, PlayerProfileSnapshot profile) throws SQLException {
        stmt.setString(1, normalizeKey(profile.name()));
        stmt.setString(2, profile.name().trim());
        stmt.setInt(3, profile.points());
        stmt.setInt(4, profile.speedLevel());
        stmt.setInt(5, profile.jumpLevel());
        stmt.setInt(6, profile.kickLevel());
        stmt.setInt(7, profile.lifeLevel());
        stmt.setInt(8, profile.matchesPlayed());
        stmt.setInt(9, profile.wins());
        stmt.setInt(10, profile.goalsFor());
        stmt.setInt(11, profile.goalsAgainst());
    }

    private PlayerProfileSnapshot mapResultSetToSnapshot(ResultSet rs) throws SQLException {
        return new PlayerProfileSnapshot(
                rs.getString("name"),
                rs.getInt("points"),
                rs.getInt("speed_level"),
                rs.getInt("jump_level"),
                rs.getInt("kick_level"),
                rs.getInt("life_level"),
                rs.getInt("matches_played"),
                rs.getInt("wins"),
                rs.getInt("goals_for"),
                rs.getInt("goals_against")
        );
    }

    private boolean isInvalidName(String playerName) {
        return playerName == null || playerName.isBlank();
    }

    private String normalizeKey(String playerName) {
        return playerName.trim().toLowerCase();
    }
}
