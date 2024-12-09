package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Centralizes logic for a player’s combat state, including their Doppel.
 * Handles damage, logout, doppel creation, and session interactions.
 */
public class CombatEntity {
    private final KillionCombatLog plugin;
    @Getter
    private final UUID playerId;
    /**
     * -- SETTER --
     *  Called when this player's actual Player object changes (e.g. on join).
     */
    @Setter
    @Getter
    private Player player; // Current online player (if online)
    @Getter
    private Doppel doppel;
    // Active sessions involving this entity
    private final Set<CombatSession> activeSessions = new HashSet<>();

    public CombatEntity(KillionCombatLog plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerId = player.getUniqueId();
    }

    public boolean hasDoppel() {
        return doppel != null;
    }

    /**
     * Called when the player takes damage from another player.
     */
    public void handlePlayerDamage(Player attacker) {
        if (attacker.getUniqueId().equals(playerId)) return;

        CombatEntity attackerEntity = plugin.getEntityManager().getEntity(attacker);
        handleCombatSession(attackerEntity);
    }

    /**
     * Called when the Doppel takes damage from another player.
     */
    public void handleDoppelDamage(Player attacker) {
        if (attacker.getUniqueId().equals(playerId)) return;

        CombatEntity attackerEntity = plugin.getEntityManager().getEntity(attacker);
        // Handle session creation or reengagement
        CombatSession session = handleCombatSession(attackerEntity);

        if (session != null) {
            // Reengage to extend the timer, simulating re-engagement/doppel swap logic
            session.handleReengagement(attacker.getUniqueId());
        }
    }

    /**
     * Handles creating or re-engaging a combat session with the given attacker.
     * Returns the session involved.
     */
    private CombatSession handleCombatSession(CombatEntity attackerEntity) {
        if (attackerEntity == null || attackerEntity == this) return null;

        CombatSession existingSession = getSessionWith(attackerEntity.getPlayerId());
        if (existingSession == null) {
            // Create a new session
            CombatSession newSession = plugin.getCombatSessionManager().createSession(attackerEntity, this);
            attackerEntity.addSession(newSession);
            this.addSession(newSession);

            // If either entity wasn't in combat before, trigger events
            if (!attackerEntity.isInCombat() && attackerEntity.getPlayer() != null) {
                plugin.getServer().getPluginManager().callEvent(new PlayerCombatStateChangedEvent(attackerEntity.getPlayer(), true));
            }
            if (!isInCombat() && getPlayer() != null) {
                plugin.getServer().getPluginManager().callEvent(new PlayerCombatStateChangedEvent(getPlayer(), true));
            }

            return newSession;
        } else {
            // Session exists, just re-engage it
            existingSession.handleReengagement(attackerEntity.getPlayerId());
            return existingSession;
        }
    }

    /**
     * Called when the player logs out.
     * If in PvP zone, spawn doppel. Otherwise, just clear sessions.
     */
    public void handleLogout(boolean isPvPZone) {
        if (!isPvPZone) {
            // Non-pvp zone logout: just end sessions gracefully
            for (CombatSession session : new HashSet<>(activeSessions)) {
                session.forceEnd(CombatEndReason.TIMER_EXPIRED);
                // The session manager + combat manager will handle cleanup
                plugin.getCombatManager().endCombatSession(session, CombatEndReason.TIMER_EXPIRED);
            }
            activeSessions.clear();
            return;
        }

        // In PvP zone, create Doppel
        if (player != null) {
            this.doppel = new Doppel(player.getUniqueId(), player.getHealth(), player.getInventory().getContents());
            plugin.getNPCManager().createNPC(doppel, player);
            LogUtil.debug("Doppel created for player " + player.getName());
        }
    }

    /**
     * Called when player re-logs in while Doppel exists.
     * Transfers Doppel state back to player and remove Doppel.
     */
    public void handleLoginWithDoppel(Player player) {
        this.player = player;
        if (doppel != null) {
            doppel.transferToPlayer(player);
            removeDoppel();

            // If still in combat, notify player
            if (isInCombat()) {
                plugin.getServer().getPluginManager().callEvent(
                        new PlayerCombatStateChangedEvent(player, true)
                );

                int highestRemainingTime = activeSessions.stream()
                        .mapToInt(CombatSession::getRemainingSeconds)
                        .max().orElse(0);
                String message = plugin.getConfigManager().getStillInCombatMessage()
                        .replace("%seconds%", String.valueOf(highestRemainingTime));
                player.sendMessage(co.killionrevival.killioncombatlog.util.MessageUtility.chatComponent(message));
            }
        }
    }

    /**
     * Removes the Doppel if it exists.
     */
    public void removeDoppel() {
        if (doppel != null) {
            // Remove the Doppel's NPC using NPCManager
            plugin.getNPCManager().removeNPC(doppel.getNpc());
            doppel = null;
        }
    }


    public void handleSafeZoneEntry() {
        // Force end all sessions
        for (CombatSession session : new HashSet<>(activeSessions)) {
            session.forceEnd(CombatEndReason.ENTERED_SAFE_ZONE);
            plugin.getCombatManager().endCombatSession(session, CombatEndReason.ENTERED_SAFE_ZONE);
        }
        activeSessions.clear();
    }

    public boolean isInCombat() {
        return !activeSessions.isEmpty();
    }

    public CombatSession getSessionWith(UUID otherId) {
        return activeSessions.stream()
                .filter(session -> session.hasEntity(otherId))
                .findFirst()
                .orElse(null);
    }

    public void addSession(CombatSession session) {
        activeSessions.add(session);
    }

    public void removeSession(CombatSession session) {
        activeSessions.remove(session);
    }

    public Set<CombatSession> getActiveSessionsSnapshot() {
        return new HashSet<>(activeSessions);
    }
}
