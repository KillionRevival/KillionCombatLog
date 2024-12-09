package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages the ticking and tracking of combat sessions.
 * Handles combat display updates and session lifecycle.
 */
public class CombatSessionManager {
    private final KillionCombatLog plugin;
    private final int initialDuration;
    private final int reengagementDuration;
    private final int doppelSwapDuration;
    private final int maxDuration;
    private final long maxSessionLength;

    private final Set<CombatSession> activeSessions = new HashSet<>();
    private BukkitTask tickTask;

    public CombatSessionManager(KillionCombatLog plugin,
                                int initialDuration,
                                int reengagementDuration,
                                int doppelSwapDuration,
                                int maxDuration,
                                long maxSessionLength) {
        this.plugin = plugin;
        this.initialDuration = initialDuration;
        this.reengagementDuration = reengagementDuration;
        this.doppelSwapDuration = doppelSwapDuration;
        this.maxDuration = maxDuration;
        this.maxSessionLength = maxSessionLength;
    }

    public CombatSession createSession(CombatEntity combatant, CombatEntity victim) {
        // Ensure no duplicate sessions for the same pair
        if (combatant.getSessionWith(victim.getPlayerId()) != null) {
            // Session already exists, just return that
            return combatant.getSessionWith(victim.getPlayerId());
        }

        CombatSession session = new CombatSession(
                combatant,
                victim,
                initialDuration,
                reengagementDuration,
                doppelSwapDuration,
                maxDuration,
                maxSessionLength
        );

        activeSessions.add(session);
        startTickTask();
        LogUtil.debug(String.format("Created new combat session between %s and %s",
                combatant.getPlayerId(), victim.getPlayerId()));

        // Initial combat display update
        updateCombatDisplays(session);

        return session;
    }

    public void removeSession(CombatSession session) {
        activeSessions.remove(session);
        LogUtil.debug("Removed combat session from tracking");

        if (activeSessions.isEmpty() && tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    private void startTickTask() {
        if (tickTask != null) return;

        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                Set<CombatSession> sessionsToRemove = new HashSet<>();

                for (CombatSession session : activeSessions) {
                    CombatEndReason reason = session.tick();

                    // Update displays for both players
                    updateCombatDisplays(session);

                    if (reason != CombatEndReason.NONE) {
                        sessionsToRemove.add(session);
                        plugin.getCombatManager().endCombatSession(session, reason);
                    }
                }

                sessionsToRemove.forEach(CombatSessionManager.this::removeSession);

                if (activeSessions.isEmpty()) {
                    cancel();
                    tickTask = null;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void updateCombatDisplays(CombatSession session) {
        Player combatant = plugin.getServer().getPlayer(session.getCombatantId());
        Player victim = plugin.getServer().getPlayer(session.getVictimId());

        if (combatant != null) {
            plugin.getCombatManager().updateCombatDisplay(combatant);
        }
        if (victim != null) {
            plugin.getCombatManager().updateCombatDisplay(victim);
        }
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        for (CombatSession session : new HashSet<>(activeSessions)) {
            plugin.getCombatManager().endCombatSession(session, CombatEndReason.TIMER_EXPIRED);
        }
        activeSessions.clear();
        LogUtil.info("Combat session manager shutdown complete");
    }

    public Set<CombatSession> getActiveSessions() {
        return new HashSet<>(activeSessions);
    }
}
