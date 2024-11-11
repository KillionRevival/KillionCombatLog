package co.killionrevival.killionCombatLog.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a player combat logs (disconnects during combat).
 */
public class PlayerCombatLogEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;

    /**
     * Constructor to create a new PlayerCombatLogEvent.
     *
     * @param player The player who combat logged.
     */
    public PlayerCombatLogEvent(Player player) {
        this.player = player;
    }

    /**
     * Gets the player who combat logged.
     *
     * @return The player.
     */
    public Player getPlayer() {
        return this.player;
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
