package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages the creation and tracking of combat sessions and their timers
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

    public CombatSessionManager(
            KillionCombatLog plugin,
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

    /**
     * Creates a new combat session between two entities if one doesn't already exist
     * @return The new or existing session between these entities
     */
    public CombatSession createSession(CombatEntity combatant, CombatEntity victim) {
        // Check if a session already exists between these entities
        CombatSession existingSession = combatant.getSessionWith(victim.getEntityId());
        if (existingSession != null) {
            return existingSession;
        }

        // Create new session
        CombatSession session = new CombatSession(
                combatant,
                victim,
                initialDuration,
                reengagementDuration,
                doppelSwapDuration,
                maxDuration,
                maxSessionLength
        );

        // Add to both entities
        if (combatant.addCombatSession(session) && victim.addCombatSession(session)) {
            activeSessions.add(session);
            startTickTask();
            LogUtil.debug(String.format("Created new combat session between %s and %s",
                    combatant.getEntityId(), victim.getEntityId()));
        }

        return session;
    }

    /**
     * Removes a session from tracking
     */
    public void removeSession(CombatSession session) {
        activeSessions.remove(session);
        LogUtil.debug("Removed combat session from tracking");

        // Stop tick task if no more sessions
        if (activeSessions.isEmpty() && tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Starts the timer tick task if not already running
     */
    private void startTickTask() {
        if (tickTask != null) return;

        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                Set<CombatSession> sessionsToRemove = new HashSet<>();

                for (CombatSession session : activeSessions) {
                    CombatEndReason reason = session.tick();
                    if (reason != CombatEndReason.NONE) {
                        sessionsToRemove.add(session);
                    }
                }

                // Clean up ended sessions
                sessionsToRemove.forEach(CombatSessionManager.this::removeSession);

                // Stop task if no more sessions
                if (activeSessions.isEmpty()) {
                    cancel();
                    tickTask = null;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Cleans up all sessions
     */
    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        activeSessions.clear();
        LogUtil.info("Combat session manager shutdown complete");
    }

    /**
     * Gets all active combat sessions
     */
    public Set<CombatSession> getActiveSessions() {
        return new HashSet<>(activeSessions);
    }
}