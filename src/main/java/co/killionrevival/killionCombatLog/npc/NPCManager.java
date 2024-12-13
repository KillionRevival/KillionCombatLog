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
        Location spawnLocation = doppel.getLastLocation();

        LogUtil.debug(String.format("Creating NPC Doppel for player %s at precise location X: %.6f, Y: %.6f, Z: %.6f, Yaw: %.6f, Pitch: %.6f",
                originalPlayer.getName(),
                spawnLocation.getX(),
                spawnLocation.getY(),
                spawnLocation.getZ(),
                spawnLocation.getYaw(),
                spawnLocation.getPitch()));

        try {
            NPC npc = npcRegistry.createNPC(EntityType.PLAYER, originalPlayer.getName());

            // Configure NPC settings using proper metadata
            npc.setProtected(false);

            // Prevent movement
            npc.data().set(NPC.Metadata.COLLIDABLE.getKey(), false);
            npc.data().set(NPC.Metadata.FLUID_PUSHABLE.getKey(), false);
            npc.data().set("combat-npc", true);
            npc.data().set(NPC.Metadata.PATHFINDER_FALL_DISTANCE.getKey(), 0.0);
            npc.data().set(NPC.Metadata.KNOCKBACK.getKey(), false);

            // Force spawn the NPC with exact positioning
            boolean spawned = npc.spawn(spawnLocation);
            if (!spawned) {
                LogUtil.error("Failed to spawn NPC at location", null);
                return null;
            }

            // Ensure exact positioning
            if (npc.getEntity() != null) {
                npc.getEntity().teleport(spawnLocation);

                if (npc.getEntity() instanceof Player npcPlayer) {
                    // Set health - MOVED THIS BEFORE INVENTORY TO ENSURE IT'S SET
                    AttributeInstance maxHealthAttr = originalPlayer.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealthAttr != null) {
                        double maxHealth = maxHealthAttr.getValue();
                        AttributeInstance npcMaxHealth = npcPlayer.getAttribute(Attribute.MAX_HEALTH);
                        if (npcMaxHealth != null) {
                            npcMaxHealth.setBaseValue(maxHealth);
                            LogUtil.debug(String.format("Setting NPC max health to: %f", maxHealth));
                        }
                    }

                    // Set the actual health value from the doppel
                    npcPlayer.setHealth(doppel.getHealth());
                    LogUtil.debug(String.format("Setting NPC current health to: %f", doppel.getHealth()));

                    // Lock to exact coordinates and disable gravity
                    npcPlayer.teleport(spawnLocation);
                    npcPlayer.setGravity(false);

                    // Set basic entity properties
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

                    // Force update inventory
                    npcPlayer.updateInventory();

                    // Store the health in NPC data as backup
                    npc.data().set("original-health", doppel.getHealth());
                }
            }

            // Add CombatLogTrait
            CombatLogTrait trait = npc.getOrAddTrait(CombatLogTrait.class);
            trait.setParentPlayer(originalPlayer);

            npc.data().set("owner-uuid", originalPlayer.getUniqueId());
            doppel.setNPC(npc);

            LogUtil.combat(String.format("Created Doppel NPC for player %s with ID %s at precise location",
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
