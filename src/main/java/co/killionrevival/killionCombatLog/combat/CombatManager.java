package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the combat states of players and handles persistence for players who die due to combat logging.
 * Uses an SQLite database to store data across server restarts.
 */
public class CombatManager {

    private final Map<UUID, Integer> combat = new HashMap<>();
    private final KillionCombatLog plugin;
    private Connection connection;

    /**
     * Constructor to initialize the manager with the main plugin instance.
     * Sets up the database connection.
     *
     * @param plugin The main plugin instance.
     */
    public CombatManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        this.setupDatabase();
    }

    /**
     * Sets up the SQLite database for storing player death information.
     */
    private void setupDatabase() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "deadplayers.db");
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS dead_players (UUID TEXT PRIMARY KEY, KillerName TEXT)")) {
                stmt.executeUpdate();
            }
            plugin.getLogger().info("Database setup completed successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Database setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the database connection when the plugin is disabled.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks a player as dead or alive in the database.
     * This method is asynchronous to prevent blocking the main server thread.
     *
     * @param playerUUID The UUID of the player.
     * @param killerName The name of the killer, if applicable.
     * @param isDead     True to mark the player as dead; false to mark as alive.
     */
    public void setPlayerAsDead(UUID playerUUID, String killerName, boolean isDead) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PreparedStatement stmt = isDead
                    ? connection.prepareStatement("INSERT OR REPLACE INTO dead_players (UUID, KillerName) VALUES (?, ?)")
                    : connection.prepareStatement("DELETE FROM dead_players WHERE UUID = ?")) {
                stmt.setString(1, playerUUID.toString());
                if (isDead) {
                    stmt.setString(2, killerName);
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Checks if a player is marked as dead in the database.
     *
     * @param playerUUID The UUID of the player.
     * @return True if the player is marked as dead; false otherwise.
     */
    public boolean isPlayerDead(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM dead_players WHERE UUID = ?")) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves the killer's name for a player who died while offline.
     *
     * @param playerUUID The UUID of the player.
     * @return The killer's name, or null if not found.
     */
    public String getKillerName(UUID playerUUID) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT KillerName FROM dead_players WHERE UUID = ?")) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("KillerName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a player to the combat list and resets their combat timer.
     *
     * @param player The player to add.
     */
    public void addPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        int combatDuration = plugin.getConfig().getInt("settings.combat-tag-duration");

        if (combat.containsKey(playerId)) {
            this.resetTimeRemain(player);
            return;
        }

        combat.put(playerId, combatDuration);
        plugin.getServer().getPluginManager().callEvent(new PlayerCombatStateChangedEvent(player, true));
    }

    /**
     * Removes a player from the combat list and triggers a state change event.
     *
     * @param player The player to remove.
     */
    public void removePlayer(Player player) {
        if (!combat.containsKey(player.getUniqueId())) {
            return;
        }

        combat.remove(player.getUniqueId());
        plugin.getServer().getPluginManager().callEvent(new PlayerCombatStateChangedEvent(player, false));
    }

    /**
     * Checks if a player is currently in combat.
     *
     * @param player The player to check.
     * @return True if the player is in combat; false otherwise.
     */
    public boolean isInCombat(Player player) {
        return player != null && combat.containsKey(player.getUniqueId());
    }

    /**
     * Gets the remaining combat time for a player.
     *
     * @param player The player to check.
     * @return The remaining time in seconds, or -1 if not in combat.
     */
    public int getTimeRemain(Player player) {
        return player != null ? combat.getOrDefault(player.getUniqueId(), -1) : -1;
    }

    /**
     * Resets the combat timer for a player.
     *
     * @param player The player whose timer to reset.
     */
    public void resetTimeRemain(Player player) {
        if (player == null) {
            return;
        }

        int combatDuration = plugin.getConfig().getInt("settings.combat-tag-duration");
        combat.put(player.getUniqueId(), combatDuration);
        plugin.getServer().getPluginManager().callEvent(new PlayerCombatStateChangedEvent(player, true));
    }

    /**
     * Decreases the combat timer for a player by one second.
     *
     * @param player The player whose timer to decrease.
     */
    public void decreaseTimeRemain(Player player) {
        UUID playerId = player.getUniqueId();
        int seconds = combat.getOrDefault(playerId, 0);
        combat.put(playerId, seconds - 1);
    }

    /**
     * Sets the combat timer for a player to a specific value.
     *
     * @param player The player whose timer to set.
     * @param time   The time in seconds to set.
     */
    public void setTimeRemain(Player player, int time) {
        combat.put(player.getUniqueId(), time);
    }
}
