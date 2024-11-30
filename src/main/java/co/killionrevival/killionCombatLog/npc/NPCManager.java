package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
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
        LogUtil.info("NPC Manager initialized with Citizens API");
    }

    /**
     * Creates an NPC to represent a player who combat logged.
     * Copies the player's appearance and inventory to the NPC.
     *
     * @param player The player to represent.
     * @return The created NPC.
     */
    public NPC createNPC(Player player) {
        LogUtil.debug(String.format("Creating NPC for player %s at location %s",
            player.getName(), player.getLocation()));

        // Create an NPC with the player's name and type
        NPC npc = npcRegistry.createNPC(player.getType(), player.getName());
        npc.spawn(player.getLocation());
        LogUtil.debug("NPC spawned successfully");

        // Configure movement speed
        npc.getNavigator().getDefaultParameters().baseSpeed(0.8f);

        // Copy equipment to the NPC
        LogUtil.debug("Copying player equipment to NPC...");
        Equipment equipment = npc.getTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HELMET, player.getInventory().getHelmet());
        equipment.set(Equipment.EquipmentSlot.CHESTPLATE, player.getInventory().getChestplate());
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, player.getInventory().getLeggings());
        equipment.set(Equipment.EquipmentSlot.BOOTS, player.getInventory().getBoots());
        equipment.set(Equipment.EquipmentSlot.HAND, player.getInventory().getItemInMainHand());
        equipment.set(Equipment.EquipmentSlot.OFF_HAND, player.getInventory().getItemInOffHand());

        // Copy inventory contents to the NPC
        LogUtil.debug("Copying player inventory to NPC...");
        Inventory npcInventory = npc.getTrait(Inventory.class);
        ItemStack[] playerInventory = player.getInventory().getContents();
        for (int i = 0; i < playerInventory.length; i++) {
            ItemStack item = playerInventory[i];
            if (item != null) {
                npcInventory.setItem(i, item);
            }
        }
        LogUtil.combat(String.format("Created combat log NPC for player %s with ID %s",
                player.getName(), npc.getUniqueId()));
        return npc;
    }

    /**
     * Retrieves an NPC by its UUID.
     *
     * @param npcUniqueId The UUID of the NPC.
     * @return The NPC, or null if not found.
     */
    public NPC getNPC(UUID npcUniqueId) {
        NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(npcUniqueId);
        LogUtil.debug(String.format("Retrieved NPC with UUID %s: %s",
                npcUniqueId, npc != null ? "found" : "not found"));
        return npc;
    }

    /**
     * Removes an NPC by despawning and destroying it.
     *
     * @param npc The NPC to remove.
     */
    public void removeNPC(NPC npc) {
        if (npc != null) {
            LogUtil.debug(String.format("Removing NPC: ID=%s, Name=%s", npc.getUniqueId(), npc.getName()));
            npc.despawn();
            npc.destroy();
            LogUtil.combat(String.format("Removed combat log NPC: %s", npc.getUniqueId()));
        }
    }

    /**
     * Removes an NPC by its UUID.
     *
     * @param npcUniqueId The UUID of the NPC.
     */
    public void removeNPC(UUID npcUniqueId) {
        LogUtil.debug(String.format("Attempting to remove NPC with UUID: %s", npcUniqueId));
        NPC npc = getNPC(npcUniqueId);
        removeNPC(npc);
    }
}
