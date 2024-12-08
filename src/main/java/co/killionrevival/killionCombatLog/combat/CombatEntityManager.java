package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.util.LogUtil;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all CombatEntity instances and their lifecycle
 */
public class CombatEntityManager {
    private final Map<UUID, CombatEntity> entities = new ConcurrentHashMap<>();

    /**
     * Gets or creates a CombatEntity for a player
     */
    public CombatEntity getEntity(Player player) {
        return entities.computeIfAbsent(player.getUniqueId(),
                k -> {
                    LogUtil.debug("Creating new CombatEntity for player: " + player.getName());
                    return new CombatEntity(player);
                });
    }

    /**
     * Gets an entity by UUID if it exists
     */
    public CombatEntity getEntity(UUID id) {
        CombatEntity entity = entities.get(id);
        if (entity == null) {
            LogUtil.debug("No CombatEntity found for UUID: " + id);
        }
        return entity;
    }

    /**
     * Removes an entity and cleans up their combat sessions
     */
    public void removeEntity(UUID id) {
        CombatEntity entity = entities.remove(id);
        if (entity != null) {
            LogUtil.debug("Removing CombatEntity and ending all combat sessions for: " + id);
            // End all combat sessions
            for (CombatSession session : new HashSet<>(entity.getActiveSessions())) {
                session.forceEnd(CombatEndReason.TIMER_EXPIRED);
            }
        }
    }

    /**
     * Creates a Doppel for a player if in a valid zone
     */
    public Doppel createDoppel(Player player, boolean isPvPZone) {
        if (!isPvPZone) {
            LogUtil.debug("Cannot create Doppel - player not in PvP zone: " + player.getName());
            return null;
        }

        CombatEntity entity = getEntity(player);
        if (entity.hasDoppel()) {
            LogUtil.debug("Doppel already exists for player: " + player.getName());
            return null;
        }

        LogUtil.debug("Creating Doppel for player: " + player.getName());
        return entity.createDoppel(player);
    }

    /**
     * Gets all managed entities
     */
    public Collection<CombatEntity> getAllEntities() {
        return new ArrayList<>(entities.values());
    }

    /**
     * Checks if an entity is managed
     */
    public boolean hasEntity(UUID id) {
        return entities.containsKey(id);
    }

    /**
     * Shutdown and cleanup
     */
    public void shutdown() {
        LogUtil.debug("Shutting down CombatEntityManager...");
        entities.values().forEach(entity ->
                entity.handleSafeZoneEntry());
        entities.clear();
        LogUtil.info("CombatEntityManager shutdown complete");
    }
}