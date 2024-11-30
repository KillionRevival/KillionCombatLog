package co.killionrevival.killioncombatlog.combat.persistence;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.persistence.repository.ICombatStateRepository;
import co.killionrevival.killioncombatlog.combat.persistence.repository.DeathRecord;
import co.killionrevival.killioncombatlog.combat.persistence.repository.SQLiteCombatRepository;
import lombok.extern.java.Log;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Manages the persistence of combat-related data using a repository pattern.
 */
@Log
public class CombatPersistenceManager {

    private final KillionCombatLog plugin;
    private final ICombatStateRepository repository;

    /**
     * Constructor to initialize the persistence manager.
     *
     * @param plugin The main plugin instance
     */
    public CombatPersistenceManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        try {
            this.repository = new SQLiteCombatRepository(plugin);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize combat persistence", e);
        }
    }

    /**
     * Records a player's death in the database.
     *
     * @param playerUUID The UUID of the player
     * @param killerName The name of the killer
     */
    public void recordPlayerDeath(UUID playerUUID, String killerName) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        String location = player != null ? formatLocation(player.getLocation()) : "unknown";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                repository.savePlayerDeath(playerUUID, killerName, location));
    }

    /**
     * Removes a death record from the database.
     *
     * @param playerUUID The UUID of the player
     */
    public void removeDeathRecord(UUID playerUUID) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                repository.removeDeathRecord(playerUUID));
    }

    /**
     * Checks if a player has a death record.
     *
     * @param playerUUID The UUID of the player
     * @return true if the player has a death record
     */
    public boolean hasDeathRecord(UUID playerUUID) {
        return repository.hasDeathRecord(playerUUID);
    }

    /**
     * Gets the killer's name from a player's death record.
     *
     * @param playerUUID The UUID of the player
     * @return The killer's name, or null if not found
     */
    public String getKillerName(UUID playerUUID) {
        return repository.getDeathRecord(playerUUID)
                .map(DeathRecord::getKillerName)
                .orElse(null);
    }

    /**
     * Cleans up old death records.
     *
     * @param maxAgeHours Maximum age of records to keep in hours
     */
    public void cleanupOldRecords(int maxAgeHours) {
        long maxAgeMillis = maxAgeHours * 3600000L;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int removed = repository.cleanupOldRecords(maxAgeMillis);
            if (removed > 0) {
                log.info("Cleaned up " + removed + " old death records");
            }
        });
    }

    /**
     * Formats a location for storage.
     *
     * @param location The location to format
     * @return Formatted location string
     */
    private String formatLocation(Location location) {
        return String.format("%s,%d,%d,%d",
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    /**
     * Shuts down the persistence manager and closes resources.
     */
    public void shutdown() {
        repository.close();
    }
}