package co.killionrevival.killioncombatlog.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class DoppelState {
    private final UUID playerUUID;
    private final double health;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final long creationTime;
    private final String lastLocation;

    public boolean isExpired(long maxDuration) {
        return System.currentTimeMillis() - creationTime > maxDuration * 1000;
    }
}