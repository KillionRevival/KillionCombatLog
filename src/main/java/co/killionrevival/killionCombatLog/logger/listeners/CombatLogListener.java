package co.killionrevival.killioncombatlog.logger.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.logger.events.PlayerCombatLogEvent;
import co.killionrevival.killioncombatlog.combat.CombatEntity;
import co.killionrevival.killioncombatlog.combat.Doppel;
import co.killionrevival.killioncombatlog.npc.traits.CombatLogTrait;
import co.killionrevival.killioncombatlog.util.LogUtil;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handles combat logging events and manages Doppel creation
 */
public class CombatLogListener implements Listener {
    private final KillionCombatLog plugin;

    public CombatLogListener(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCombatLog(PlayerCombatLogEvent event) {
        Player player = event.getPlayer();
        CombatEntity entity = plugin.getEntityManager().getEntity(player);

        if (!isPvPEnabled(player.getLocation())) {
            entity.handleSafeZoneEntry();
            return;
        }

        // Create Doppel
        Doppel doppel = entity.createDoppel(player);
        if (doppel == null) {
            LogUtil.warn("Failed to create Doppel for player: " + player.getName());
            return;
        }

        // Create NPC using NPCManager
        NPC npc = plugin.getNPCManager().createNPC(doppel, player);

        // Add required traits
        CombatLogTrait combatLogTrait = npc.getOrAddTrait(CombatLogTrait.class);
        combatLogTrait.setParentPlayer(player);

        // Set up hologram
        npc.getOrAddTrait(HologramTrait.class);

        // Configure NPC metadata
        npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.data().set(NPC.Metadata.TEXT_DISPLAY_COMPONENT, true);
        npc.data().set(NPC.Metadata.ACTIVATION_RANGE, 100);

        // Ensure NPC is spawned
        if (!npc.isSpawned()) {
            npc.spawn(player.getLocation());
        }

        // Track the Doppel's NPC
        plugin.getCombatLogManager().addPlayer(player.getUniqueId(), npc.getUniqueId());
        LogUtil.debug(String.format("Created combat log NPC for player %s", player.getName()));
    }

    private boolean isPvPEnabled(Location location) {
        // TODO: Implement WorldGuard check
        return true;
    }
}