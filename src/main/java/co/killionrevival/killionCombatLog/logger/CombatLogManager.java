package co.killionrevival.killioncombatlog.logger;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages death records for players who died while offline (Doppels killed).
 * No longer tracks NPCs or active combat loggers directly since that is handled elsewhere.
 */
public class CombatLogManager {
    private final Map<UUID, String> deathRecords = new HashMap<>();
    private final KillionCombatLog plugin;

    public CombatLogManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        LogUtil.info("Combat Log Manager initialized");
    }

    /**
     * Records a player's death while offline (Doppel scenario).
     */
    public void recordPlayerDeath(UUID playerUUID, String killerName, String location) {
        // We only need to store killerName since location is just for debugging or was previously used.
        deathRecords.put(playerUUID, killerName);
        LogUtil.debug(String.format("Recorded death for player %s, killed by %s at %s",
                playerUUID, killerName, location));
    }

    /**
     * Removes a player's death record when they rejoin and the death is applied.
     */
    public void removeDeathRecord(UUID playerUUID) {
        deathRecords.remove(playerUUID);
        LogUtil.debug(String.format("Removed death record for player %s", playerUUID));
    }

    /**
     * Checks if a player has a pending death record (died while offline).
     */
    public boolean hasDeathRecord(UUID playerUUID) {
        return deathRecords.containsKey(playerUUID);
    }

    /**
     * Gets the killer's name from a player's death record.
     */
    public String getKillerName(UUID playerUUID) {
        return deathRecords.get(playerUUID);
    }
}
