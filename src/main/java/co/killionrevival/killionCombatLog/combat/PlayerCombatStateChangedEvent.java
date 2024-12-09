package co.killionrevival.killioncombatlog.combat;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered when a player's combat state changes.
 */
@Getter
public class PlayerCombatStateChangedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final boolean inCombat;

    public PlayerCombatStateChangedEvent(Player player, boolean inCombat) {
        this.player = player;
        this.inCombat = inCombat;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
