package co.killionrevival.killionCombatLog.listener;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killionCombatLog.managers.LogoutManager;
import co.killionrevival.killionCombatLog.npc.LoggedOutPlayer;
import co.killionrevival.killioncommons.KillionCommons;
import co.killionrevival.killioncommons.KillionUtilities;
import co.killionrevival.killioncommons.npc.NpcManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {
    final KillionUtilities utils;
    final LogoutManager manager;
    final NpcManager npcManager;

    public DisconnectListener() {
        this.manager = KillionCombatLog.getInstance()
                                       .getLogoutManager();
        this.utils = KillionCombatLog.getCommons();
        this.npcManager = KillionCommons.getInstance()
                                        .getNpcManager();
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        final PlayerQuitEvent.QuitReason reason = event.getReason();
        final Player player = event.getPlayer();
        // if player was kicked and was recently disconnected by the logout manager, we don't need to do anything
        if (reason.equals(PlayerQuitEvent.QuitReason.KICKED) && manager.hasLoggedOutSuccessfully(player)) {
            return;
        }
        // Otherwise, spawn an NPC with their details.
        npcManager.spawn(
                new LoggedOutPlayer(player.getUniqueId(), player.getName()),
                event.getPlayer().getLocation()
        );
    }
}
