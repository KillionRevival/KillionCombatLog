package co.killionrevival.killioncombatlog.combat.timer;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages combat timers for players, handling countdown tasks and timer state.
 * This centralizes timer management that was previously spread across multiple classes.
 */
@RequiredArgsConstructor
public class CombatTimerManager {
    private final KillionCombatLog plugin;
    private final Map<UUID, BukkitRunnable> activeTimers = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> remainingTimes = new ConcurrentHashMap<>();

    /**
     * Starts or resets a combat timer for a player.
     *
     * @param player The player to start/reset the timer for
     * @return true if a new timer was started, false if an existing timer was reset
     */
    public boolean startTimer(Player player) {
        UUID playerId = player.getUniqueId();
        boolean isNew = !activeTimers.containsKey(playerId);

        // Cancel existing timer if present
        stopTimer(player);

        // Get combat duration from config
        int duration = plugin.getConfig().getInt("settings.combat-tag-duration");
        remainingTimes.put(playerId, duration);

        // Create and start new timer
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                int remaining = remainingTimes.get(playerId);

                if (remaining <= 0) {
                    endCombat(player);
                    return;
                }

                // Update remaining time
                remainingTimes.put(playerId, remaining - 1);

                // Send action bar message
                String message = plugin.getConfig().getString("messages.in-combat", "&cIn Combat: &f%seconds%s");
                message = message.replace("%seconds%", String.valueOf(remaining));
                player.sendActionBar(MessageUtility.chatComponent(message));
            }
        };

        // Store and start the timer
        activeTimers.put(playerId, timer);
        timer.runTaskTimer(plugin, 0L, 20L);

        return isNew;
    }

    /**
     * Stops the combat timer for a player.
     *
     * @param player The player whose timer should be stopped
     */
    public void stopTimer(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable timer = activeTimers.remove(playerId);

        if (timer != null) {
            timer.cancel();
        }

        remainingTimes.remove(playerId);
    }

    /**
     * Gets the remaining combat time for a player.
     *
     * @param player The player to check
     * @return The remaining time in seconds, or -1 if not in combat
     */
    public int getRemainingTime(Player player) {
        return remainingTimes.getOrDefault(player.getUniqueId(), -1);
    }

    /**
     * Checks if a player is currently in combat.
     *
     * @param player The player to check
     * @return true if the player has an active combat timer
     */
    public boolean isInCombat(Player player) {
        return player != null && activeTimers.containsKey(player.getUniqueId());
    }

    /**
     * Ends combat for a player, cleaning up resources and triggering appropriate events.
     *
     * @param player The player whose combat should end
     */
    private void endCombat(Player player) {
        UUID playerId = player.getUniqueId();

        // Cancel and remove timer
        BukkitRunnable timer = activeTimers.remove(playerId);
        if (timer != null) {
            timer.cancel();
        }

        // Clean up remaining time
        remainingTimes.remove(playerId);

        // Send exit combat message
        String exitMessage = plugin.getConfig().getString("messages.no-longer", "&aYou are no longer in combat.");
        player.sendActionBar(MessageUtility.chatComponent(exitMessage));

        // Trigger combat state changed event
        plugin.getServer().getPluginManager().callEvent(
                new PlayerCombatStateChangedEvent(player, false)
        );
    }

    /**
     * Cleans up all timers. Should be called on plugin disable.
     */
    public void shutdown() {
        for (BukkitRunnable timer : activeTimers.values()) {
            timer.cancel();
        }
        activeTimers.clear();
        remainingTimes.clear();
    }
}