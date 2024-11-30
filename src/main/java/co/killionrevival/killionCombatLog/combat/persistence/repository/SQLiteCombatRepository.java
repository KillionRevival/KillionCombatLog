package co.killionrevival.killioncombatlog.combat.persistence.repository;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import lombok.extern.java.Log;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * SQLite implementation of the ICombatStateRepository.
 */
@Log
public class SQLiteCombatRepository implements ICombatStateRepository {

    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS dead_players (
            UUID TEXT PRIMARY KEY,
            KillerName TEXT,
            Location TEXT,
            DeathTimestamp BIGINT,
            UNIQUE(UUID)
        )
    """;

    private static final String INSERT_DEATH_SQL =
            "INSERT OR REPLACE INTO dead_players (UUID, KillerName, Location, DeathTimestamp) VALUES (?, ?, ?, ?)";

    private static final String SELECT_DEATH_SQL =
            "SELECT * FROM dead_players WHERE UUID = ?";

    private static final String DELETE_DEATH_SQL =
            "DELETE FROM dead_players WHERE UUID = ?";

    private static final String COUNT_DEATH_SQL =
            "SELECT COUNT(*) FROM dead_players WHERE UUID = ?";

    private static final String CLEANUP_OLD_SQL =
            "DELETE FROM dead_players WHERE DeathTimestamp < ?";

    private final Connection connection;

    /**
     * Creates a new SQLite repository instance.
     *
     * @param plugin The plugin instance
     * @throws SQLException if database initialization fails
     */
    public SQLiteCombatRepository(KillionCombatLog plugin) throws SQLException {
        LogUtil.info("Initializing combat database connection...");
        File dbFile = new File(plugin.getDataFolder(), "combat_data.db");
        boolean needsInit = !dbFile.exists();

        try {
            if (!dbFile.getParentFile().exists()) {
                LogUtil.debug("Creating plugin data folder");
                dbFile.getParentFile().mkdirs();
            }

            LogUtil.debug(String.format("Connecting to SQLite database: %s", dbFile.getAbsolutePath()));
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            connection.setAutoCommit(true);

            if (needsInit) {
                LogUtil.info("First run detected, creating database tables...");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(CREATE_TABLE_SQL);
                }
                LogUtil.info("Database tables created successfully");
            }

            LogUtil.info("Combat database initialized successfully");
        } catch (SQLException e) {
            LogUtil.error("Failed to initialize combat database", e);
            throw e;
        }
    }

    @Override
    public void savePlayerDeath(UUID playerUUID, String killerName, String location) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_DEATH_SQL)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, killerName);
            stmt.setString(3, location);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
            LogUtil.debug(String.format("Saved death record: Player=%s, Killer=%s, Location=%s",
                playerUUID, killerName, location));
        } catch (SQLException e) {
            LogUtil.error("Failed to save player death record", e);
        }
    }

    @Override
    public Optional<DeathRecord> getDeathRecord(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_DEATH_SQL)) {
            stmt.setString(1, playerUUID.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DeathRecord record = DeathRecord.builder()
                            .playerUUID(playerUUID)
                            .killerName(rs.getString("KillerName"))
                            .location(rs.getString("Location"))
                            .timestamp(Instant.ofEpochMilli(rs.getLong("DeathTimestamp")))
                            .build();
                    LogUtil.debug(String.format("Retrieved death record for player %s", playerUUID));
                    return Optional.of(record);
                }
            }
        } catch (SQLException e) {
            LogUtil.error("Failed to retrieve death record", e);
        }
        return Optional.empty();
    }

    @Override
    public void removeDeathRecord(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_DEATH_SQL)) {
            stmt.setString(1, playerUUID.toString());
            int affected = stmt.executeUpdate();
            LogUtil.debug(String.format("Removed death record for player %s: %d rows affected",
                playerUUID, affected));
        } catch (SQLException e) {
            LogUtil.error("Failed to remove death record", e);
        }
    }

    @Override
    public boolean hasDeathRecord(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement(COUNT_DEATH_SQL)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasRecord = rs.next() && rs.getInt(1) > 0;
                LogUtil.debug(String.format("Checked death record existence for player %s: %s",
                    playerUUID, hasRecord));
                return hasRecord;
            }
        } catch (SQLException e) {
            LogUtil.error("Failed to check death record existence", e);
            return false;
        }
    }

    @Override
    public int cleanupOldRecords(long maxAgeMillis) {
        try (PreparedStatement stmt = connection.prepareStatement(CLEANUP_OLD_SQL)) {
            stmt.setLong(1, System.currentTimeMillis() - maxAgeMillis);
            int removed = stmt.executeUpdate();
            LogUtil.info(String.format("Cleaned up %d old death records", removed));
            return removed;
        } catch (SQLException e) {
            LogUtil.error("Failed to cleanup old records", e);
            return 0;
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                LogUtil.debug("Closing database connection");
                connection.close();
                LogUtil.info("Combat database connection closed successfully");
            }
        } catch (SQLException e) {
            LogUtil.error("Failed to close combat database", e);
        }
    }
}