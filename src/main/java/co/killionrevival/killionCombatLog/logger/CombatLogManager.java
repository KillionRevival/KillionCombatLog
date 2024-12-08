package co.killionrevival.killioncombatlog.logger;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages players who have combat logged and their associated NPCs.
 */
public class CombatLogManager {
    private final Map<UUID, UUID> combatLoggers = new HashMap<>();
    private final Map<UUID, String> deathRecords = new HashMap<>();
    private final KillionCombatLog plugin;

    public CombatLogManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        LogUtil.info("Combat Log Manager initialized");
    }

    /**
     * Adds a player to the combat loggers list with their associated NPC UUID
     */
    public void addPlayer(UUID playerUniqueId, UUID npcUniqueId) {
        this.combatLoggers.put(playerUniqueId, npcUniqueId);
        LogUtil.combat(String.format("Added combat logger: Player UUID=%s, NPC UUID=%s",
                playerUniqueId, npcUniqueId));
    }

    /**
     * Removes a player from the combat loggers list and removes their NPC
     */
    public void removePlayer(UUID playerUniqueId) {
        if (!this.isCombatLogger(playerUniqueId)) {
            LogUtil.debug(String.format("Attempted to remove non-existent combat logger: %s", playerUniqueId));
            return;
        }

        LogUtil.combat(String.format("Removing combat logger: Player UUID=%s", playerUniqueId));
        UUID npcUUID = this.combatLoggers.get(playerUniqueId);
        plugin.getNPCManager().removeNPC(npcUUID);
        this.combatLoggers.remove(playerUniqueId);
        LogUtil.debug(String.format("Successfully removed combat logger and associated NPC: Player=%s, NPC=%s",
                playerUniqueId, npcUUID));
    }

    /**
     * Records a player's death while logged out
     */
    public void recordPlayerDeath(UUID playerUUID, String killerName, String location) {
        deathRecords.put(playerUUID, killerName);
        LogUtil.debug(String.format("Recorded death for player %s, killed by %s at %s",
                playerUUID, killerName, location));
    }

    /**
     * Removes a player's death record
     */
    public void removeDeathRecord(UUID playerUUID) {
        deathRecords.remove(playerUUID);
        LogUtil.debug(String.format("Removed death record for player %s", playerUUID));
    }

    /**
     * Checks if a player has a death record
     */
    public boolean hasDeathRecord(UUID playerUUID) {
        return deathRecords.containsKey(playerUUID);
    }

    /**
     * Gets the killer's name from a player's death record
     */
    public String getKillerName(UUID playerUUID) {
        return deathRecords.get(playerUUID);
    }

    /**
     * Checks if a player is currently a combat logger
     */
    public boolean isCombatLogger(UUID playerUniqueId) {
        boolean isLogger = this.combatLoggers.containsKey(playerUniqueId) &&
                this.combatLoggers.get(playerUniqueId) != null;
        LogUtil.debug(String.format("Checked combat logger status for %s: %s", playerUniqueId, isLogger));
        return isLogger;
    }

    /**
     * Gets the NPC UUID associated with a combat logging player
     */
    public UUID getPlayerNPC(UUID playerUniqueId) {
        UUID npcUUID = this.combatLoggers.get(playerUniqueId);
        LogUtil.debug(String.format("Retrieved NPC UUID for player %s: %s", playerUniqueId, npcUUID));
        return npcUUID;
    }
}