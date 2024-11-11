package co.killionrevival.killionCombatLog.listeners;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killionCombatLog.events.PlayerCombatLogEvent;
import co.killionrevival.killionCombatLog.traits.CombatLogTrait;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for handling events related to combat logging.
 * Specifically, it handles the creation of NPCs when players combat log.
 */
public class CombatLogListener implements Listener {

    private final KillionCombatLog plugin;

    /**
     * Constructor to initialize the listener with the main plugin instance.
     *
     * @param plugin The main plugin instance.
     */
    public CombatLogListener(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    /**
     * Event handler for when a player combat logs.
     * Creates an NPC to represent the player who disconnected during combat.
     *
     * @param event The PlayerCombatLogEvent.
     */
    @EventHandler
    public void onCombatLog(PlayerCombatLogEvent event) {
        Player player = event.getPlayer();

        // Create an NPC representing the player
        NPC npc = plugin.getNPCManager().createNPC(player);

        // Add custom traits to the NPC
        CombatLogTrait combatLogTrait = npc.getOrAddTrait(CombatLogTrait.class);
        combatLogTrait.setParentPlayer(player);
        combatLogTrait.setAbsorption(player.getAbsorptionAmount());

        npc.getOrAddTrait(HologramTrait.class);

        // Configure NPC data properties
        npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.data().set(NPC.Metadata.TEXT_DISPLAY_COMPONENT, true);
        npc.data().set(NPC.Metadata.ACTIVATION_RANGE, 100);

        // Spawn the NPC at the player's location
        npc.spawn(player.getLocation());

        // Track the NPC in the CombatLogManager
        plugin.getCombatLogManager().addPlayer(player.getUniqueId(), npc.getUniqueId());
    }
}
