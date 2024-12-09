package co.killionrevival.killioncombatlog.logger;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.database.DeathRecord;
import co.killionrevival.killioncombatlog.util.LogUtil;

import java.util.UUID;

public class CombatLogManager {
    private final KillionCombatLog plugin;

    public CombatLogManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        LogUtil.info("Combat Log Manager initialized");
    }

    public void recordPlayerDeath(UUID playerUUID, String killerName, String location) {
        plugin.getDatabaseManager().saveDeathRecord(playerUUID, killerName, location);
        LogUtil.debug(String.format("Recorded death for player %s, killed by %s at %s",
                playerUUID, killerName, location));
    }

    public void removeDeathRecord(UUID playerUUID) {
        plugin.getDatabaseManager().removeDeathRecord(playerUUID);
        LogUtil.debug(String.format("Removed death record for player %s", playerUUID));
    }

    public boolean hasDeathRecord(UUID playerUUID) {
        DeathRecord record = plugin.getDatabaseManager().loadDeathRecord(playerUUID);
        return record != null && !record.isExpired(plugin.getConfigManager().getMaxSessionLength());
    }

    public String getKillerName(UUID playerUUID) {
        DeathRecord record = plugin.getDatabaseManager().loadDeathRecord(playerUUID);
        return record != null ? record.getKillerName() : null;
    }
}
