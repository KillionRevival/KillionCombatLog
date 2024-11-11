package co.killionrevival.killionCombatLog.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a player's combat state changes (enters or exits combat).
 */
public class PlayerCombatStateChangedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final boolean inCombat;

    /**
     * Constructor to create a new PlayerCombatStateChangedEvent.
     *
     * @param player   The player whose combat state changed.
     * @param inCombat True if the player is now in combat; false otherwise.
     */
    public PlayerCombatStateChangedEvent(Player player, boolean inCombat) {
        this.player = player;
        this.inCombat = inCombat;
    }

    /**
     * Gets the player whose combat state changed.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Determines if the player is now in combat.
     *
     * @return True if in combat; false otherwise.
     */
    public boolean isInCombat() {
        return this.inCombat;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Static method required by Bukkit to get the handler list.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
