package co.killionrevival.killioncombatlog.combat;

import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents the offline stand-in (NPC) for a player who logged out in a PvP zone.
 */
@Getter
public class Doppel {
    private final UUID ownerId;
    private NPC npc;
    private final double health;
    private final ItemStack[] inventoryContents;
    private final Location lastLocation;  // Added precise location storage

    public Doppel(UUID ownerId, double health, ItemStack[] inventoryContents, Location location) {
        this.ownerId = ownerId;
        this.health = health;
        this.inventoryContents = inventoryContents.clone();
        this.lastLocation = location.clone();  // Store exact location
    }

    public void setNPC(NPC npc) {
        this.npc = npc;
    }

    public void transferToPlayer(Player player) {
        player.setHealth(health);

        // If NPC has inventory trait, transfer from NPC, else fallback to original contents
        if (npc != null && npc.hasTrait(net.citizensnpcs.api.trait.trait.Inventory.class)) {
            net.citizensnpcs.api.trait.trait.Inventory npcInv = npc.getOrAddTrait(net.citizensnpcs.api.trait.trait.Inventory.class);
            ItemStack[] contents = npcInv.getContents();

            player.getInventory().clear();
            int limit = Math.min(contents.length, player.getInventory().getSize());
            for (int i = 0; i < limit; i++) {
                if (contents[i] != null) {
                    player.getInventory().setItem(i, contents[i]);
                }
            }
        } else {
            // Fallback to original contents if no NPC inventory
            player.getInventory().clear();
            int limit = Math.min(inventoryContents.length, player.getInventory().getSize());
            for (int i = 0; i < limit; i++) {
                if (inventoryContents[i] != null) {
                    player.getInventory().setItem(i, inventoryContents[i]);
                }
            }
        }
    }
}
