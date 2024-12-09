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

        try {
            NPC npc = npcRegistry.createNPC(EntityType.PLAYER, originalPlayer.getName());

            // Force spawn the NPC and verify
            boolean spawned = npc.spawn(originalPlayer.getLocation());
            if (!spawned) {
                LogUtil.error("Failed to spawn NPC at location", null);
                return null;
            }

            LogUtil.debug("NPC spawned successfully");

            if (npc.getEntity() instanceof Player npcPlayer) {
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

                // Copy equipment directly to NPC player
                npcPlayer.getInventory().setArmorContents(originalPlayer.getInventory().getArmorContents());
                npcPlayer.getInventory().setContents(originalPlayer.getInventory().getContents());
                npcPlayer.getInventory().setItemInMainHand(originalPlayer.getInventory().getItemInMainHand().clone());
                npcPlayer.getInventory().setItemInOffHand(originalPlayer.getInventory().getItemInOffHand().clone());

                LogUtil.debug("Copied inventory and equipment to NPC");
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
