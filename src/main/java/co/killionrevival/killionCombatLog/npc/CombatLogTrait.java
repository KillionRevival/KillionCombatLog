package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.CombatSession;
import co.killionrevival.killioncombatlog.util.LogUtil;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.Setter;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;

public class CombatLogTrait extends Trait implements Listener {
    private final KillionCombatLog plugin = KillionCombatLog.getPlugin(KillionCombatLog.class);
    @Setter
    private Player parentPlayer;
    private HologramTrait hologramTrait;
    private BukkitRunnable updateTask;

    public CombatLogTrait() {
        super("CombatLogTrait");
    }

    @Override
    public void onAttach() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupHologram();
        startUpdateTask();
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

        double hearts = Math.ceil(npcPlayer.getHealth() / 2.0);

        hologramTrait.clear();
        hologramTrait.addLine(MessageUtility.colorize(String.format("&c❤ %d Hearts", (int)hearts)));
        hologramTrait.addLine(MessageUtility.colorize(String.format("&e%ds Remaining", remainingTime)));
        hologramTrait.addLine(MessageUtility.colorize("&c&lPLAYER DISCONNECTED"));
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

    @EventHandler
    public void onNPCDeath(NPCDeathEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        LogUtil.debug("Doppel death event triggered");

        if (event.getNPC().getEntity() instanceof Player npcPlayer) {
            getNPC().data().set("final-health", 0.0);

            // Drop inventory contents
            for (ItemStack item : npcPlayer.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    npcPlayer.getWorld().dropItemNaturally(npcPlayer.getLocation(), item.clone());
                }
            }

            // Drop armor contents
            for (ItemStack item : npcPlayer.getInventory().getArmorContents()) {
                if (item != null && !item.getType().isAir()) {
                    npcPlayer.getWorld().dropItemNaturally(npcPlayer.getLocation(), item.clone());
                }
            }
        }

        UUID ownerId = event.getNPC().data().get("owner-uuid");
        String killerName = (event.getNPC().getEntity() instanceof Player npcPlayer && npcPlayer.getKiller() != null) ?
            npcPlayer.getKiller().getName() : "unknown";

        if (ownerId != null) {
            LogUtil.debug(String.format("Recording death for Doppel owner %s, killed by %s",
                ownerId, killerName));
            plugin.getCombatLogManager().recordPlayerDeath(
                ownerId,
                killerName,
                event.getNPC().getStoredLocation().toString()
            );
        }

        getNPC().despawn();
        getNPC().destroy();
    }
}