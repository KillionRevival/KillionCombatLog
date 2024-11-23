package co.killionrevival.killioncombatlog.logger;

import co.killionrevival.killioncombatlog.KillionCombatLog;

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
    }

    /**
     * Adds a player to the combat loggers list with their associated NPC UUID.
     *
     * @param playerUniqueId The UUID of the player.
     * @param npcUniqueId    The UUID of the NPC representing the player.
     */
    public void addPlayer(UUID playerUniqueId, UUID npcUniqueId) {
        this.combatLoggers.put(playerUniqueId, npcUniqueId);
    }

    /**
     * Removes a player from the combat loggers list and removes their NPC.
     *
     * @param playerUniqueId The UUID of the player.
     */
    public void removePlayer(UUID playerUniqueId) {
        if (!this.isCombatLogger(playerUniqueId)) {
            return;
        }

        plugin.getNPCManager().removeNPC(this.combatLoggers.get(playerUniqueId));
        this.combatLoggers.remove(playerUniqueId);
    }

    /**
     * Checks if a player is currently a combat logger.
     *
     * @param playerUniqueId The UUID of the player.
     * @return True if the player is a combat logger; false otherwise.
     */
    public boolean isCombatLogger(UUID playerUniqueId) {
        return this.combatLoggers.containsKey(playerUniqueId) && this.combatLoggers.get(playerUniqueId) != null;
    }

    /**
     * Retrieves the NPC UUID associated with a combat logging player.
     *
     * @param playerUniqueId The UUID of the player.
     * @return The UUID of the NPC.
     */
    public UUID getPlayerNPC(UUID playerUniqueId) {
        return this.combatLoggers.get(playerUniqueId);
    }
}
