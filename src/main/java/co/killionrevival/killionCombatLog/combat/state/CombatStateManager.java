package co.killionrevival.killioncombatlog.combat.state;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import co.killionrevival.killioncombatlog.combat.timer.CombatTimerManager;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Manages the in-memory combat states of players.
 * Handles all real-time combat state transitions and timer management.
 */
public class CombatStateManager {

    private final KillionCombatLog plugin;
    @Getter
    private final CombatTimerManager timerManager;

    /**
     * Constructor to initialize the state manager.
     *
     * @param plugin The main plugin instance
     */
    public CombatStateManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        this.timerManager = new CombatTimerManager(plugin);
    }

    /**
     * Adds a player to combat state and starts their combat timer.
     *
     * @param player The player to add to combat
     */
    public void enterCombat(Player player) {
        if (player == null) return;

        boolean isNewCombat = timerManager.startTimer(player);
        if (isNewCombat) {
            plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(player, true)
            );
        }
    }

    /**
     * Removes a player from combat state and stops their combat timer.
     *
     * @param player The player to remove from combat
     */
    public void exitCombat(Player player) {
        if (player == null) return;

        if (isInCombat(player)) {
            timerManager.stopTimer(player);
            plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(player, false)
            );
        }
    }

    /**
     * Checks if a player is currently in combat.
     *
     * @param player The player to check
     * @return true if the player is in combat
     */
    public boolean isInCombat(Player player) {
        return timerManager.isInCombat(player);
    }

    /**
     * Gets the remaining combat time for a player.
     *
     * @param player The player to check
     * @return The remaining time in seconds, or -1 if not in combat
     */
    public int getRemainingTime(Player player) {
        return timerManager.getRemainingTime(player);
    }

    /**
     * Resets the combat timer for a player.
     *
     * @param player The player whose timer to reset
     */
    public void resetCombatTimer(Player player) {
        timerManager.startTimer(player);
    }

    /**
     * Sets the combat timer for a player to a specific value.
     *
     * @param player The player whose timer to set
     * @param time The time in seconds
     */
    public void setCombatTime(Player player, int time) {
        if (time <= 0) {
            exitCombat(player);
        } else {
            enterCombat(player);
        }
    }

    /**
     * Cleans up resources and stops all timers.
     * Should be called when the plugin is disabled.
     */
    public void shutdown() {
        timerManager.shutdown();
    }
}