package co.killionrevival.killioncombatlog.combat.persistence.repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for combat state persistence operations.
 */
public interface ICombatStateRepository {

    /**
     * Records a player's death in the persistence layer.
     *
     * @param playerUUID The UUID of the dead player
     * @param killerName The name of the killer
     * @param location The location where the death occurred
     */
    void savePlayerDeath(UUID playerUUID, String killerName, String location);

    /**
     * Retrieves death record for a player.
     *
     * @param playerUUID The UUID of the player
     * @return Optional containing death record if found
     */
    Optional<DeathRecord> getDeathRecord(UUID playerUUID);

    /**
     * Removes a player's death record.
     *
     * @param playerUUID The UUID of the player
     */
    void removeDeathRecord(UUID playerUUID);

    /**
     * Checks if a player has a death record.
     *
     * @param playerUUID The UUID of the player
     * @return true if player has a death record
     */
    boolean hasDeathRecord(UUID playerUUID);

    /**
     * Cleans up old death records.
     *
     * @param maxAgeMillis Maximum age of records to keep
     * @return Number of records removed
     */
    int cleanupOldRecords(long maxAgeMillis);

    /**
     * Closes the repository and cleans up resources.
     */
    void close();
}