package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.KillionCombatLog;
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
import java.util.UUID;

/**
 * Handles NPC doppel combat logic. On damage, notifies the owner's CombatEntity.
 */
public class CombatLogTrait extends Trait implements Listener {

    private final KillionCombatLog plugin = KillionCombatLog.getPlugin(KillionCombatLog.class);
    @Setter
    private Player parentPlayer; // The player who owns this Doppel

    private HologramTrait hologramTrait;

    public CombatLogTrait() {
        super("CombatLogTrait");
    }

    @Override
    public void onAttach() {
        setupHologram();
    }

    @Override
    public void onRemove() {
        clearHologram();
    }

    @EventHandler
    public void onNPCDamage(NPCDamageByEntityEvent event) {
        if (event.getNPC() != this.getNPC()) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        event.setCancelled(false); // allow damage
        UUID ownerId = event.getNPC().data().get("owner-uuid");
        if (ownerId != null) {
            // Notify owner's CombatEntity
            var ownerEntity = plugin.getEntityManager().getEntity(ownerId);
            if (ownerEntity != null) {
                ownerEntity.handleDoppelDamage(attacker);
            }
        }
    }

    @EventHandler
    public void onNPCDeath(NPCDeathEvent event) {
        if (event.getNPC() != this.getNPC()) return;

        Player npcPlayer = (event.getNPC().getEntity() instanceof Player) ? (Player) event.getNPC().getEntity() : null;
        if (npcPlayer != null) {
            dropPlayerInventory(npcPlayer);
        }

        getNPC().despawn();
        getNPC().destroy();

        UUID ownerId = event.getNPC().data().get("owner-uuid");
        String killerName = (npcPlayer != null && npcPlayer.getKiller() != null) ? npcPlayer.getKiller().getName() : "unknown";
        if (ownerId != null) {
            plugin.getCombatLogManager().recordPlayerDeath(ownerId, killerName, event.getNPC().getStoredLocation().toString());
        }
    }

    private void dropPlayerInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
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
        if (hologramTrait == null) return;
        hologramTrait.clear();
        hologramTrait.addLine(MessageUtility.colorize("&c&lDISCONNECTED PLAYER"));
        hologramTrait.addLine(MessageUtility.colorize("&aAttack to claim loot!"));
    }
}
