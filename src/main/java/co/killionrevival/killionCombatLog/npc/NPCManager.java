package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.Doppel;
import co.killionrevival.killioncombatlog.util.LogUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.*;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Manages the creation and removal of NPCs (Doppels).
 */
public class NPCManager {
    private final KillionCombatLog plugin;
    private final NPCRegistry npcRegistry;

    public NPCManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        this.npcRegistry = CitizensAPI.getNPCRegistry();
        LogUtil.info("NPC Manager initialized with Citizens API");
    }

    public NPC createNPC(Doppel doppel, Player originalPlayer) {
        LogUtil.debug(String.format("Creating NPC Doppel for player %s at %s",
                originalPlayer.getName(), originalPlayer.getLocation()));

        NPC npc = npcRegistry.createNPC(EntityType.PLAYER, originalPlayer.getName());
        if (!npc.isSpawned()) {
            npc.spawn(originalPlayer.getLocation());
        }

        if (npc.getEntity() instanceof Player npcPlayer) {
            AttributeInstance maxHealthAttr = originalPlayer.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttr != null) {
                double maxHealth = maxHealthAttr.getValue();
                AttributeInstance npcMaxHealth = npcPlayer.getAttribute(Attribute.MAX_HEALTH);
                if (npcMaxHealth != null) {
                    npcMaxHealth.setBaseValue(maxHealth);
                }
            }
            npcPlayer.setHealth(doppel.getHealth());
        }

        npc.getNavigator().getDefaultParameters().baseSpeed(0.8f);

        // Copy equipment
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HELMET, originalPlayer.getInventory().getHelmet());
        equipment.set(Equipment.EquipmentSlot.CHESTPLATE, originalPlayer.getInventory().getChestplate());
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, originalPlayer.getInventory().getLeggings());
        equipment.set(Equipment.EquipmentSlot.BOOTS, originalPlayer.getInventory().getBoots());
        equipment.set(Equipment.EquipmentSlot.HAND, originalPlayer.getInventory().getItemInMainHand());
        equipment.set(Equipment.EquipmentSlot.OFF_HAND, originalPlayer.getInventory().getItemInOffHand());

        // Copy inventory
        Inventory npcInv = npc.getOrAddTrait(Inventory.class);
        ItemStack[] playerInventory = originalPlayer.getInventory().getContents();
        for (int i = 0; i < playerInventory.length; i++) {
            ItemStack item = playerInventory[i];
            if (item != null) {
                npcInv.setItem(i, item);
            }
        }

        npc.data().set("owner-uuid", originalPlayer.getUniqueId());

        doppel.setNPC(npc);

        LogUtil.combat(String.format("Created Doppel NPC for player %s with ID %s",
                originalPlayer.getName(), npc.getUniqueId()));
        return npc;
    }

    public NPC getNPC(UUID npcUniqueId) {
        NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(npcUniqueId);
        LogUtil.debug(String.format("Retrieved NPC with UUID %s: %s",
                npcUniqueId, npc != null ? "found" : "not found"));
        return npc;
    }

    public void removeNPC(NPC npc) {
        if (npc != null) {
            LogUtil.debug(String.format("Removing NPC: ID=%s, Name=%s", npc.getUniqueId(), npc.getName()));
            npc.despawn();
            npc.destroy();
            LogUtil.combat(String.format("Removed Doppel NPC: %s", npc.getUniqueId()));
        }
    }

    public void removeNPC(UUID npcUniqueId) {
        LogUtil.debug(String.format("Attempting to remove NPC with UUID: %s", npcUniqueId));
        NPC npc = getNPC(npcUniqueId);
        removeNPC(npc);
    }
}
