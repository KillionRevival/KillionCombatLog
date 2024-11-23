package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.*;
import net.citizensnpcs.api.trait.trait.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Manages the creation and removal of NPCs representing combat loggers.
 * Uses the Citizens API to handle NPC functionalities.
 */
public class NPCManager {

    private final KillionCombatLog plugin;
    private final NPCRegistry npcRegistry;

    /**
     * Constructor to initialize the manager with the main plugin instance.
     *
     * @param plugin The main plugin instance.
     */
    public NPCManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        this.npcRegistry = CitizensAPI.getNPCRegistry();
    }

    /**
     * Creates an NPC to represent a player who combat logged.
     * Copies the player's appearance and inventory to the NPC.
     *
     * @param player The player to represent.
     * @return The created NPC.
     */
    public NPC createNPC(Player player) {
        // Create an NPC with the player's name and type
        NPC npc = npcRegistry.createNPC(player.getType(), player.getName());

        // Spawn the NPC at the player's location
        npc.spawn(player.getLocation());
        npc.getNavigator().getDefaultParameters().baseSpeed(0.8f);

        // Copy equipment to the NPC
        Equipment equipment = npc.getTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HELMET, player.getInventory().getHelmet());
        equipment.set(Equipment.EquipmentSlot.CHESTPLATE, player.getInventory().getChestplate());
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, player.getInventory().getLeggings());
        equipment.set(Equipment.EquipmentSlot.BOOTS, player.getInventory().getBoots());
        equipment.set(Equipment.EquipmentSlot.HAND, player.getInventory().getItemInMainHand());
        equipment.set(Equipment.EquipmentSlot.OFF_HAND, player.getInventory().getItemInOffHand());

        // Copy inventory contents to the NPC
        Inventory npcInventory = npc.getTrait(Inventory.class);
        ItemStack[] playerInventory = player.getInventory().getContents();
        for (int i = 0; i < playerInventory.length; i++) {
            ItemStack item = playerInventory[i];
            if (item != null) {
                npcInventory.setItem(i, item);
            }
        }

        return npc;
    }

    /**
     * Retrieves an NPC by its UUID.
     *
     * @param npcUniqueId The UUID of the NPC.
     * @return The NPC, or null if not found.
     */
    public NPC getNPC(UUID npcUniqueId) {
        return CitizensAPI.getNPCRegistry().getByUniqueId(npcUniqueId);
    }

    /**
     * Removes an NPC by despawning and destroying it.
     *
     * @param npc The NPC to remove.
     */
    public void removeNPC(NPC npc) {
        if (npc != null) {
            npc.despawn();
            npc.destroy();
        }
    }

    /**
     * Removes an NPC by its UUID.
     *
     * @param npcUniqueId The UUID of the NPC.
     */
    public void removeNPC(UUID npcUniqueId) {
        NPC npc = getNPC(npcUniqueId);
        removeNPC(npc);
    }
}
