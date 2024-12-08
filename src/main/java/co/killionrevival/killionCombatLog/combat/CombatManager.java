package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class CombatManager {
    private final KillionCombatLog plugin;

    public void initiateCombat(Player attacker, Player victim) {
        CombatEntity attackerEntity = plugin.getEntityManager().getEntity(attacker);
        CombatEntity victimEntity = plugin.getEntityManager().getEntity(victim);

        CombatSession session = plugin.getCombatSessionManager().createSession(
                attackerEntity,
                victimEntity
        );

        if (!attackerEntity.isInCombat()) {
            plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(attacker, true)
            );
        }
        if (!victimEntity.isInCombat()) {
            plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(victim, true)
            );
        }
    }

    public void handlePlayerDeath(Player deadPlayer, Player killer) {
        CombatEntity deadEntity = plugin.getEntityManager().getEntity(deadPlayer);

        for (CombatSession session : deadEntity.getActiveSessions()) {
            CombatEndReason reason = session.handleDeath(deadEntity.getEntityId());
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

    private void endCombatSession(CombatSession session, CombatEndReason reason) {
        CombatEntity entity1 = plugin.getEntityManager().getEntity(session.getCombatantId());
        CombatEntity entity2 = plugin.getEntityManager().getEntity(session.getVictimId());

        if (entity1 != null) {
            entity1.removeCombatSession(session);
            if (!entity1.isInCombat()) {
                Player player = plugin.getServer().getPlayer(entity1.getEntityId());
                if (player != null) {
                    plugin.getServer().getPluginManager().callEvent(
                            new PlayerCombatStateChangedEvent(player, false)
                    );
                }
            }
        }

        if (entity2 != null) {
            entity2.removeCombatSession(session);
            if (!entity2.isInCombat()) {
                Player player = plugin.getServer().getPlayer(entity2.getEntityId());
                if (player != null) {
                    plugin.getServer().getPluginManager().callEvent(
                            new PlayerCombatStateChangedEvent(player, false)
                    );
                }
            }
        }
    }

    public void handleSafeZoneEntry(Player player) {
        CombatEntity entity = plugin.getEntityManager().getEntity(player);
        entity.handleSafeZoneEntry();

        if (entity.isInCombat()) {
            plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(player, false)
            );
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
        // Nothing to clean up directly
    }
}