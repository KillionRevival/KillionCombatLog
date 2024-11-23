package co.killionrevival.killioncombatlog.combat.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a player's combat state changes (enters or exits combat).
 */
@Getter
public class PlayerCombatStateChangedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    /**
     * -- GETTER --
     *  Gets the player whose combat state changed.
     */
    private final Player player;
    /**
     * -- GETTER --
     *  Determines if the player is now in combat.
     */
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
