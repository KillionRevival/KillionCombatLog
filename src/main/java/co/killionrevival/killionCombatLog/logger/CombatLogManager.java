package co.killionrevival.killioncombatlog.logger;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages players who have combat logged and their associated NPCs.
 * Keeps track of combat loggers to handle their NPCs appropriately.
 */
public class CombatLogManager {

    private final Map<UUID, UUID> combatLoggers = new HashMap<>();
    private final KillionCombatLog plugin;

    /**
     * Constructor to initialize the manager with the main plugin instance.
     *
     * @param plugin The main plugin instance.
     */
    public CombatLogManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        LogUtil.info("Combat Log Manager initialized");
    }

    /**
     * Adds a player to the combat loggers list with their associated NPC UUID.
     *
     * @param playerUniqueId The UUID of the player.
     * @param npcUniqueId    The UUID of the NPC representing the player.
     */
    public void addPlayer(UUID playerUniqueId, UUID npcUniqueId) {
        this.combatLoggers.put(playerUniqueId, npcUniqueId);
        LogUtil.combat(String.format("Added combat logger: Player UUID=%s, NPC UUID=%s",
            playerUniqueId, npcUniqueId));
    }

    /**
     * Removes a player from the combat loggers list and removes their NPC.
     *
     * @param playerUniqueId The UUID of the player.
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
     * Checks if a player is currently a combat logger.
     *
     * @param playerUniqueId The UUID of the player.
     * @return True if the player is a combat logger; false otherwise.
     */
    public boolean isCombatLogger(UUID playerUniqueId) {
        boolean isLogger = this.combatLoggers.containsKey(playerUniqueId) &&
                          this.combatLoggers.get(playerUniqueId) != null;
        LogUtil.debug(String.format("Checked combat logger status for %s: %s", playerUniqueId, isLogger));
        return isLogger;
    }

    /**
     * Retrieves the NPC UUID associated with a combat logging player.
     *
     * @param playerUniqueId The UUID of the player.
     * @return The UUID of the NPC.
     */
    public UUID getPlayerNPC(UUID playerUniqueId) {
        UUID npcUUID = this.combatLoggers.get(playerUniqueId);
        LogUtil.debug(String.format("Retrieved NPC UUID for player %s: %s", playerUniqueId, npcUUID));
        return npcUUID;
    }
}
