package co.killionrevival.killioncombatlog.logger.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a player combat logs (disconnects during combat).
 */
@Getter
public class PlayerCombatLogEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    /**
     * -- GETTER --
     *  Gets the player who combat logged.
     *
     */
    private final Player player;

    /**
     * Constructor to create a new PlayerCombatLogEvent.
     *
     * @param player The player who combat logged.
     */
    public PlayerCombatLogEvent(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
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
