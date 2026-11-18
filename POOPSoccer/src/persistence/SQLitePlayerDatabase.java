package persistence;

import domain.Log;
import domain.Player;
import domain.PlayerDataGateway;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database and data mapper implementation for the PlayerDataGateway boundary.
 */
public class SQLitePlayerDatabase implements PlayerDataGateway {// Ayudado a implementar con Gemini Argon 4

    private static final String DEFAULT_DB_NAME = "poopsoccer.db";
    private static final String CREATE_PLAYERS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS players (
                profile_key TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                points INTEGER NOT NULL DEFAULT 0,
                speed_level INTEGER NOT NULL DEFAULT 1,
                jump_level INTEGER NOT NULL DEFAULT 1,
                kick_level INTEGER NOT NULL DEFAULT 1,
                survival_level INTEGER NOT NULL DEFAULT 1,
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
                survival_level, matches_played, wins, goals_for, goals_against, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(profile_key) DO UPDATE SET
                name = excluded.name,
                points = excluded.points,
                speed_level = excluded.speed_level,
                jump_level = excluded.jump_level,
                kick_level = excluded.kick_level,
                survival_level = excluded.survival_level,
                matches_played = excluded.matches_played,
                wins = excluded.wins,
                goals_for = excluded.goals_for,
                goals_against = excluded.goals_against,
                updated_at = CURRENT_TIMESTAMP
            """;
    private static final String SELECT_PLAYER_SQL = """
            SELECT points, speed_level, jump_level, kick_level, survival_level,
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
    public void savePlayerProgress(Player player) {
        if (isInvalidPlayer(player)) return;
        // Inserta o actualiza la fila del jugador usando parámetros seguros contra inyección SQL
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(UPSERT_PLAYER_SQL)) {
            bindPlayerToStatement(stmt, player);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Log.record(e);
        }
    }

    @Override
    public boolean loadPlayerProgress(Player player) {
        if (isInvalidPlayer(player)) return false;
        // Consulta el perfil por su clave normalizada y carga sus estadísticas en la entidad
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PLAYER_SQL)) {
            stmt.setString(1, normalizeKey(player.getName()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mapResultSetToPlayer(rs, player);
                    return true;
                }
            }
        } catch (SQLException e) {
            Log.record(e);
        }
        return false;
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
            schemaReady = true;
        }
    }

    private void bindPlayerToStatement(PreparedStatement stmt, Player player) throws SQLException {
        var profile = player.getProfile();
        stmt.setString(1, normalizeKey(player.getName()));
        stmt.setString(2, player.getName().trim());
        stmt.setInt(3, profile.getPoints());
        stmt.setInt(4, profile.getStatLevel("speed"));
        stmt.setInt(5, profile.getStatLevel("jump"));
        stmt.setInt(6, profile.getStatLevel("kick"));
        stmt.setInt(7, profile.getStatLevel("survival"));
        stmt.setInt(8, profile.getMatchesPlayed());
        stmt.setInt(9, profile.getWins());
        stmt.setInt(10, profile.getGoalsFor());
        stmt.setInt(11, profile.getGoalsAgainst());
    }

    private void mapResultSetToPlayer(ResultSet rs, Player player) throws SQLException {
        player.getProfile().loadProgress(
                rs.getInt("points"),
                rs.getInt("speed_level"),
                rs.getInt("jump_level"),
                rs.getInt("kick_level"),
                rs.getInt("survival_level"),
                rs.getInt("matches_played"),
                rs.getInt("wins"),
                rs.getInt("goals_for"),
                rs.getInt("goals_against")
        );
        player.restoreFullHealth();
    }

    private boolean isInvalidPlayer(Player player) {
        return player == null || player.getName() == null || player.getName().isBlank();
    }

    private String normalizeKey(String playerName) {
        return playerName.trim().toLowerCase();
    }
}
