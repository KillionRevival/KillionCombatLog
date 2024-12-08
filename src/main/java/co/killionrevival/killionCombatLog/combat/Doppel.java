package co.killionrevival.killioncombatlog.combat;

import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

@Getter
public class Doppel {
    private final UUID ownerId;
    private final CombatEntity entity;
    private NPC npc;
    private final double health;

    public Doppel(Player player) {
        this.ownerId = player.getUniqueId();
        this.entity = new CombatEntity(this);
        this.health = player.getHealth();
    }

    public void setNPC(NPC npc) {
        this.npc = npc;
    }

    public void transferToPlayer(Player player) {
        // Set health
        player.setHealth(health);

        // Transfer inventory if NPC exists
        if (npc != null) {
            // Get NPC inventory
            net.citizensnpcs.api.trait.trait.Inventory npcInventory = npc.getOrAddTrait(net.citizensnpcs.api.trait.trait.Inventory.class);
            ItemStack[] contents = npcInventory.getContents();

            // Clear player's current inventory
            player.getInventory().clear();

            // Transfer items
            for (int i = 0; i < contents.length && i < player.getInventory().getSize(); i++) {
                if (contents[i] != null) {
                    player.getInventory().setItem(i, contents[i]);
                }
            }
        }

        // Combat sessions are handled by the CombatEntity
    }
}