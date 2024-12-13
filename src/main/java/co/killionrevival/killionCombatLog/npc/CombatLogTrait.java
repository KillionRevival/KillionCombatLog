package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.CombatEndReason;
import co.killionrevival.killioncombatlog.combat.CombatEntity;
import co.killionrevival.killioncombatlog.combat.CombatSession;
import co.killionrevival.killioncombatlog.util.LogUtil;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.Setter;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCKnockbackEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatLogTrait extends Trait implements Listener {
    private final KillionCombatLog plugin = KillionCombatLog.getPlugin(KillionCombatLog.class);
    @Setter
    private Player parentPlayer;
    private HologramTrait hologramTrait;
    private BukkitRunnable updateTask;
    private Location lastKnownLocation;

    public CombatLogTrait() {
        super("CombatLogTrait");
    }

    @Override
    public void onAttach() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupHologram();
        startUpdateTask();
        // Store initial location
        if (getNPC().isSpawned()) {
            lastKnownLocation = getNPC().getEntity().getLocation();
        }
    }

    @Override
    public void onRemove() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        clearHologram();
    }

    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isSpawned()) {
                    cancel();
                    return;
                }
                updateHologram();
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 20L);
    }

    private void setupHologram() {
        hologramTrait = getNPC().getOrAddTrait(HologramTrait.class);
        updateHologram();
    }

    private void clearHologram() {
        if (hologramTrait != null) {
            hologramTrait.clear();
        }
    }

    private void updateHologram() {
        if (hologramTrait == null || !npc.isSpawned() || !(npc.getEntity() instanceof Player npcPlayer)) return;

        UUID ownerId = getNPC().data().get("owner-uuid");
        if (ownerId == null) return;

        var ownerEntity = plugin.getEntityManager().getEntity(ownerId);
        int remainingTime = 0;

        if (ownerEntity != null && ownerEntity.isInCombat()) {
            remainingTime = ownerEntity.getActiveSessionsSnapshot().stream()
                    .mapToInt(CombatSession::getRemainingSeconds)
                    .max()
                    .orElse(0);
        } else {
            remainingTime = plugin.getConfigManager().getDoppelDefaultDuration();
        }

        // Get current and max health
        double currentHealth = npcPlayer.getHealth();
        double maxHealth = npcPlayer.getAttribute(Attribute.MAX_HEALTH).getValue();

        // Calculate filled and empty hearts
        int totalHearts = (int) Math.ceil(maxHealth / 2.0);
        int filledHearts = (int) Math.ceil(currentHealth / 2.0);
        int emptyHearts = totalHearts - filledHearts;

        // Build heart display string
        StringBuilder heartDisplay = new StringBuilder();

        // Add filled (red) hearts
        heartDisplay.append("&c");
        for (int i = 0; i < filledHearts; i++) {
            heartDisplay.append("❤");
        }

        // Add empty (white) hearts
        heartDisplay.append("&f");
        for (int i = 0; i < emptyHearts; i++) {
            heartDisplay.append("❤");
        }

        hologramTrait.clear();
        hologramTrait.addLine(MessageUtility.colorize(heartDisplay.toString()));
        hologramTrait.addLine(MessageUtility.colorize(String.format("&e%ds Remaining", remainingTime)));
        hologramTrait.addLine(MessageUtility.colorize("&c&lPLAYER DISCONNECTED"));

        LogUtil.debug(String.format("Updated hologram for NPC %s: Health %.1f/%.1f (%d filled, %d empty hearts)",
            npc.getName(), currentHealth, maxHealth, filledHearts, emptyHearts));
    }



    @EventHandler
    public void onNPCDamage(NPCDamageByEntityEvent event) {
        if (event.getNPC() != this.getNPC()) return;
        event.setCancelled(false);

        if (event.getNPC().getEntity() instanceof Player npcPlayer) {
            // Calculate and store health after damage
            double newHealth = Math.max(0, npcPlayer.getHealth() - event.getDamage());
            LogUtil.debug(String.format("Doppel health updated: %f -> %f", npcPlayer.getHealth(), newHealth));
            getNPC().data().set("final-health", newHealth);
        }

        if (event.getDamager() instanceof Player attacker) {
            LogUtil.debug("Doppel damaged by " + attacker.getName());
            UUID ownerId = event.getNPC().data().get("owner-uuid");
            if (ownerId != null) {
                var ownerEntity = plugin.getEntityManager().getEntity(ownerId);
                if (ownerEntity != null) {
                    LogUtil.debug("Notifying owner's CombatEntity of Doppel damage");
                    ownerEntity.handleDoppelDamage(attacker);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCKnockback(NPCKnockbackEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        LogUtil.debug(String.format("Cancelling knockback on Doppel NPC: %s, Strength: %.2f, Entity: %s",
                getNPC().getName(),
                event.getStrength(),
                event.getKnockingBackEntity() != null ? event.getKnockingBackEntity().getType().name() : "null"));

        event.setCancelled(true);
    }

    @EventHandler
    public void onNPCDeath(NPCDeathEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        LogUtil.debug("Doppel death event triggered");

        // Get the death location safely
        Location deathLocation = null;
        if (event.getNPC().isSpawned()) {
            deathLocation = event.getNPC().getEntity().getLocation();
        } else if (lastKnownLocation != null) {
            deathLocation = lastKnownLocation;
        } else if (event.getNPC().getStoredLocation() != null) {
            deathLocation = event.getNPC().getStoredLocation();
        }

        // If we still don't have a location, create a safe default
        if (deathLocation == null && parentPlayer != null) {
            deathLocation = parentPlayer.getWorld().getSpawnLocation();
            LogUtil.debug("Using spawn location as fallback for death location");
        }

        if (event.getNPC().getEntity() instanceof Player npcPlayer) {
            getNPC().data().set("final-health", 0.0);
            PlayerInventory npcInv = npcPlayer.getInventory();
            Set<ItemStack> droppedItems = new HashSet<>();

            // Update last known location before processing death
            if (npcPlayer.getLocation() != null) {
                lastKnownLocation = npcPlayer.getLocation();
            }

            // Drop inventory only if we have a valid location
            if (deathLocation != null) {
                // Handle main inventory slots (0-35)
                for (int i = 0; i < 36; i++) {
                    ItemStack item = npcInv.getItem(i);
                    if (isValidItem(item) && !droppedItems.contains(item)) {
                        deathLocation.getWorld().dropItemNaturally(deathLocation, item.clone());
                        droppedItems.add(item);
                    }
                }

                // Handle armor slots if they weren't already in the main inventory
                ItemStack[] armorContents = npcInv.getArmorContents();
                for (ItemStack item : armorContents) {
                    if (isValidItem(item) && !droppedItems.contains(item)) {
                        deathLocation.getWorld().dropItemNaturally(deathLocation, item.clone());
                        droppedItems.add(item);
                    }
                }

                // Handle offhand if it wasn't already in the main inventory
                ItemStack offhandItem = npcInv.getItemInOffHand();
                if (isValidItem(offhandItem) && !droppedItems.contains(offhandItem)) {
                    deathLocation.getWorld().dropItemNaturally(deathLocation, offhandItem.clone());
                }
            }

            LogUtil.debug(String.format("Dropped %d unique items from Doppel inventory", droppedItems.size()));
        }

        UUID ownerId = event.getNPC().data().get("owner-uuid");
        String killerName = (event.getNPC().getEntity() instanceof Player npcPlayer && npcPlayer.getKiller() != null) ?
                npcPlayer.getKiller().getName() : "unknown";

        if (ownerId != null) {
            LogUtil.debug(String.format("Recording death for Doppel owner %s, killed by %s",
                    ownerId, killerName));

            // End combat sessions based on owner's role
            CombatEntity ownerEntity = plugin.getEntityManager().getEntity(ownerId);
            if (ownerEntity != null) {
                LogUtil.debug("Ending combat sessions for killed doppel's owner");

                for (CombatSession session : ownerEntity.getActiveSessionsSnapshot()) {
                    // Determine if the owner was the combatant or victim in this session
                    if (session.getCombatantId().equals(ownerId)) {
                        // Owner was the combatant (attacker), so they lost
                        LogUtil.debug("Doppel owner was combatant - ending with victim victory");
                        plugin.getCombatManager().endCombatSession(session, CombatEndReason.VICTIM_VICTORY);
                    } else if (session.getVictimId().equals(ownerId)) {
                        // Owner was the victim, so their opponent won
                        LogUtil.debug("Doppel owner was victim - ending with combatant victory");
                        plugin.getCombatManager().endCombatSession(session, CombatEndReason.COMBATANT_VICTORY);
                    }
                }
            }

            // Record the death with safe location handling
            String locationString = deathLocation != null ? deathLocation.toString() : "unknown location";
            plugin.getCombatLogManager().recordPlayerDeath(
                    ownerId,
                    killerName,
                    locationString
            );
        }

        getNPC().despawn();
        getNPC().destroy();
    }

    private boolean isValidItem(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    // Track location during updates
    @Override
    public void run() {
        if (getNPC().isSpawned()) {
            lastKnownLocation = getNPC().getEntity().getLocation();
        }
    }
}