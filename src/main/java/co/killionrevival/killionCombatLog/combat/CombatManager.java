package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * High-level combat operations:
 * - Handling death events
 * - Ending sessions
 * - Checking combat state
 * - Managing combat displays
 */
public class CombatManager {
    private final KillionCombatLog plugin;

    public CombatManager(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    public void updateCombatDisplay(Player player) {
        CombatEntity entity = plugin.getEntityManager().getEntity(player);
        if (entity == null || !entity.isInCombat()) {
            return;
        }

        // Get the highest remaining time from all active sessions
        int highestRemainingTime = entity.getActiveSessionsSnapshot().stream()
                .mapToInt(CombatSession::getRemainingSeconds)
                .max()
                .orElse(0);

        // Get and format the message
        String message = plugin.getConfigManager().getInCombatMessage()
                .replace("%seconds%", String.valueOf(highestRemainingTime));

        // Send to action bar
        player.sendActionBar(MessageUtility.chatComponent(message));
    }

    public void handlePlayerDeath(Player deadPlayer, Player killer) {
        CombatEntity deadEntity = plugin.getEntityManager().getEntity(deadPlayer);
        if (deadEntity == null) return;

        for (CombatSession session : deadEntity.getActiveSessionsSnapshot()) {
            CombatEndReason reason = session.handleDeath(deadEntity.getPlayerId());
            if (reason.isVictoryCondition()) {
                endCombatSession(session, reason);
            }
        }

        if (killer != null) {
            plugin.getCombatLogManager().recordPlayerDeath(
                    deadPlayer.getUniqueId(),
                    killer.getName(),
                    deadPlayer.getLocation().toString()
            );
        }
    }

    public void handleSafeZoneEntry(Player player) {
        CombatEntity entity = plugin.getEntityManager().getEntity(player);
        if (entity == null) return;
        entity.handleSafeZoneEntry();
    }

    public void endCombatSession(CombatSession session, CombatEndReason reason) {
        plugin.getCombatSessionManager().removeSession(session);

        CombatEntity entity1 = plugin.getEntityManager().getEntity(session.getCombatantId());
        CombatEntity entity2 = plugin.getEntityManager().getEntity(session.getVictimId());

        // Remove sessions from entities
        if (entity1 != null) {
            entity1.removeSession(session);
            if (!entity1.isInCombat()) {
                Player p = plugin.getServer().getPlayer(entity1.getPlayerId());
                if (p != null) {
                    p.sendActionBar(MessageUtility.chatComponent("")); // Clear action bar
                    plugin.getServer().getPluginManager().callEvent(
                            new PlayerCombatStateChangedEvent(p, false)
                    );
                }
            }
        }

        if (entity2 != null) {
            entity2.removeSession(session);
            if (!entity2.isInCombat()) {
                Player p = plugin.getServer().getPlayer(entity2.getPlayerId());
                if (p != null) {
                    p.sendActionBar(MessageUtility.chatComponent("")); // Clear action bar
                    plugin.getServer().getPluginManager().callEvent(
                            new PlayerCombatStateChangedEvent(p, false)
                    );
                }
            }
        }
    }

    public boolean isInCombat(Player player) {
        CombatEntity entity = plugin.getEntityManager().getEntity(player);
        return entity != null && entity.isInCombat();
    }

    public boolean isPlayerDead(UUID playerUUID) {
        return plugin.getCombatLogManager().hasDeathRecord(playerUUID);
    }

    public String getKillerName(UUID playerUUID) {
        return plugin.getCombatLogManager().getKillerName(playerUUID);
    }

    public void shutdown() {
        // No direct resources to clean here
    }
}
