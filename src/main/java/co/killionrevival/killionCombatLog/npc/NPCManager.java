package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.Doppel;
import co.killionrevival.killioncombatlog.util.LogUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.*;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class NPCManager {
    private final KillionCombatLog plugin;
    private final NPCRegistry npcRegistry;

    public NPCManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        this.npcRegistry = CitizensAPI.getNPCRegistry();
        LogUtil.info("NPC Manager initialized with Citizens API");
    }

    public NPC createNPC(Doppel doppel, Player originalPlayer) {
        Location exactLocation = originalPlayer.getLocation().clone();
        LogUtil.debug(String.format("Creating NPC Doppel for player %s at location X: %f, Y: %f, Z: %f, Yaw: %f, Pitch: %f",
                originalPlayer.getName(),
                exactLocation.getX(),
                exactLocation.getY(),
                exactLocation.getZ(),
                exactLocation.getYaw(),
                exactLocation.getPitch()));

        try {
            NPC npc = npcRegistry.createNPC(EntityType.PLAYER, originalPlayer.getName());

            // Configure NPC settings using proper metadata
            npc.setProtected(true);
            npc.data().set(NPC.Metadata.DEFAULT_PROTECTED.getKey(), false);
            npc.data().set(NPC.Metadata.DAMAGE_OTHERS.getKey(), true);

            // Prevent movement
            npc.data().set(NPC.Metadata.COLLIDABLE.getKey(), false);
            npc.data().set(NPC.Metadata.FLUID_PUSHABLE.getKey(), false);
            npc.data().set("combat-npc", true);
            npc.data().set(NPC.Metadata.PATHFINDER_FALL_DISTANCE.getKey(), 0.0);

            // Force spawn the NPC with exact positioning
            boolean spawned = npc.spawn(exactLocation);
            if (!spawned) {
                LogUtil.error("Failed to spawn NPC at location", null);
                return null;
            }

            // Ensure exact positioning
            if (npc.getEntity() != null) {
                npc.getEntity().teleport(exactLocation);
            }

            if (npc.getEntity() instanceof Player npcPlayer) {
                // Store spawn location in NPC data
                npc.data().set("spawn-location", exactLocation);

                // Set health
                AttributeInstance maxHealthAttr = originalPlayer.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double maxHealth = maxHealthAttr.getValue();
                    AttributeInstance npcMaxHealth = npcPlayer.getAttribute(Attribute.MAX_HEALTH);
                    if (npcMaxHealth != null) {
                        npcMaxHealth.setBaseValue(maxHealth);
                    }
                }
                npcPlayer.setHealth(doppel.getHealth());

                // Set basic entity properties
                npcPlayer.setGravity(false);
                npcPlayer.setFireTicks(0);
                npcPlayer.setVisualFire(false);
                npcPlayer.setInvulnerable(false);

                // Set inventory and armor directly on the NPC player
                npcPlayer.getInventory().setArmorContents(originalPlayer.getInventory().getArmorContents());
                npcPlayer.getInventory().setContents(originalPlayer.getInventory().getContents());

                // Also set using Citizens Equipment trait as a backup
                Equipment equipment = npc.getOrAddTrait(Equipment.class);
                equipment.set(Equipment.EquipmentSlot.HELMET, originalPlayer.getInventory().getHelmet());
                equipment.set(Equipment.EquipmentSlot.CHESTPLATE, originalPlayer.getInventory().getChestplate());
                equipment.set(Equipment.EquipmentSlot.LEGGINGS, originalPlayer.getInventory().getLeggings());
                equipment.set(Equipment.EquipmentSlot.BOOTS, originalPlayer.getInventory().getBoots());
                equipment.set(Equipment.EquipmentSlot.HAND, originalPlayer.getInventory().getItemInMainHand());
                equipment.set(Equipment.EquipmentSlot.OFF_HAND, originalPlayer.getInventory().getItemInOffHand());

                // Copy inventory to Citizens Inventory trait
                Inventory npcInv = npc.getOrAddTrait(Inventory.class);
                ItemStack[] playerInventory = originalPlayer.getInventory().getContents();
                for (int i = 0; i < playerInventory.length; i++) {
                    ItemStack item = playerInventory[i];
                    if (item != null) {
                        npcInv.setItem(i, item.clone());
                    }
                }

                // Force update the equipment
                npcPlayer.updateInventory();

                LogUtil.debug("Set inventory and equipment for NPC");
                LogUtil.debug("Armor contents: " + Arrays.toString(npcPlayer.getInventory().getArmorContents()));
            }

            // Add CombatLogTrait
            CombatLogTrait trait = npc.getOrAddTrait(CombatLogTrait.class);
            trait.setParentPlayer(originalPlayer);

            npc.data().set("owner-uuid", originalPlayer.getUniqueId());
            doppel.setNPC(npc);

            LogUtil.combat(String.format("Created Doppel NPC for player %s with ID %s",
                    originalPlayer.getName(), npc.getUniqueId()));
            return npc;
        } catch (Exception e) {
            LogUtil.error("Error creating NPC Doppel", e);
            return null;
        }
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
            try {
                // Force remove all traits first
                if (npc.hasTrait(CombatLogTrait.class)) {
                    npc.removeTrait(CombatLogTrait.class);
                }
                if (npc.hasTrait(Equipment.class)) {
                    npc.removeTrait(Equipment.class);
                }
                if (npc.hasTrait(Inventory.class)) {
                    npc.removeTrait(Inventory.class);
                }

                // Force despawn first
                if (npc.isSpawned()) {
                    npc.despawn();
                }

                // Then destroy
                npc.destroy();

                // Remove from registry explicitly
                npcRegistry.deregister(npc);

                LogUtil.combat(String.format("Removed Doppel NPC: %s", npc.getUniqueId()));
            } catch (Exception e) {
                LogUtil.error("Error while removing NPC", e);
            }
        }
    }

    public void removeNPC(UUID npcUniqueId) {
        LogUtil.debug(String.format("Attempting to remove NPC with UUID: %s", npcUniqueId));
        NPC npc = getNPC(npcUniqueId);
        removeNPC(npc);
    }
}
