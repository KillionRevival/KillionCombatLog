package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.state.CombatStateManager;
import co.killionrevival.killioncombatlog.combat.persistence.CombatPersistenceManager;
import co.killionrevival.killioncombatlog.combat.rules.CombatRuleEngine;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Main combat management facade that coordinates between state, persistence, and rules.
 */
@Getter
public class CombatManager {

    private final KillionCombatLog plugin;
    private final CombatStateManager stateManager;
    private final CombatPersistenceManager persistenceManager;
    /**
     * -- GETTER --
     *  Gets the combat rule engine instance.
     *
     */
    @Getter
    private final CombatRuleEngine ruleEngine;

    /**
     * Constructor to initialize the combat management system.
     *
     * @param plugin The main plugin instance
     */
    public CombatManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        this.stateManager = new CombatStateManager(plugin);
        this.persistenceManager = new CombatPersistenceManager(plugin);
        this.ruleEngine = new CombatRuleEngine(plugin);
    }

    /**
     * Adds a player to combat.
     *
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        stateManager.enterCombat(player);
    }

    /**
     * Removes a player from combat.
     *
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        stateManager.exitCombat(player);
    }

    /**
     * Checks if a player is in combat.
     *
     * @param player The player to check
     * @return true if the player is in combat
     */
    public boolean isInCombat(Player player) {
        return stateManager.isInCombat(player);
    }

    /**
     * Gets the remaining combat time for a player.
     *
     * @param player The player to check
     * @return The remaining time in seconds, or -1 if not in combat
     */
    public int getTimeRemain(Player player) {
        return stateManager.getRemainingTime(player);
    }

    /**
     * Resets a player's combat timer.
     *
     * @param player The player whose timer to reset
     */
    public void resetTimeRemain(Player player) {
        stateManager.resetCombatTimer(player);
    }

    /**
     * Sets a player's combat timer to a specific value.
     *
     * @param player The player whose timer to set
     * @param time The time in seconds
     */
    public void setTimeRemain(Player player, int time) {
        stateManager.setCombatTime(player, time);
    }

    /**
     * Marks a player as dead or alive in the persistence system.
     *
     * @param playerUUID The UUID of the player
     * @param killerName The name of the killer
     * @param isDead Whether the player should be marked as dead
     */
    public void setPlayerAsDead(UUID playerUUID, String killerName, boolean isDead) {
        if (isDead) {
            persistenceManager.recordPlayerDeath(playerUUID, killerName);
        } else {
            persistenceManager.removeDeathRecord(playerUUID);
        }
    }

    /**
     * Checks if a player is marked as dead in the persistence system.
     *
     * @param playerUUID The UUID of the player
     * @return true if the player is marked as dead
     */
    public boolean isPlayerDead(UUID playerUUID) {
        return persistenceManager.hasDeathRecord(playerUUID);
    }

    /**
     * Gets the killer's name for a dead player.
     *
     * @param playerUUID The UUID of the dead player
     * @return The killer's name, or null if not found
     */
    public String getKillerName(UUID playerUUID) {
        return persistenceManager.getKillerName(playerUUID);
    }

    /**
     * Performs cleanup when the plugin is disabled.
     */
    public void close() {
        stateManager.shutdown();
        persistenceManager.shutdown();
    }

    /**
     * Enables or disables debug mode for the combat system.
     *
     * @param debug Whether to enable debug mode
     */
    public void setDebugMode(boolean debug) {
        ruleEngine.setDebug(debug);
    }
}