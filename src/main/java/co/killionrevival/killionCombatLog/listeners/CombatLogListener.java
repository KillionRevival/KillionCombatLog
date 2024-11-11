package co.killionrevival.killionCombatLog.listeners;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killionCombatLog.events.PlayerCombatLogEvent;
import co.killionrevival.killionCombatLog.traits.CombatLogTrait;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CombatLogListener implements Listener {

    private KillionCombatLog plugin;
    public CombatLogListener(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCombatLog(PlayerCombatLogEvent event) {
        Player player = event.getPlayer();
        NPC npc = plugin.getNPCManager().createNPC(player);

        CombatLogTrait combatLogTrait = npc.getOrAddTrait(CombatLogTrait.class);
        combatLogTrait.setParentPlayer(player);
        combatLogTrait.setAbsorption(player.getAbsorptionAmount());

        npc.getOrAddTrait(HologramTrait.class);

        npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.data().set(NPC.Metadata.TEXT_DISPLAY_COMPONENT, true);
        npc.data().set(NPC.Metadata.ACTIVATION_RANGE, 100);

        npc.spawn(player.getLocation());

        plugin.getCombatLogManager().addPlayer(player.getUniqueId(), npc.getUniqueId());
    }
}