package co.killionrevival.killionCombatLog.listener;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {

    public void onPlayerDisconnect(PlayerQuitEvent event) {
        final PlayerQuitEvent.QuitReason reason = event.getReason();
        if (reason.equals(PlayerQuitEvent.QuitReason.KICKED)) {
            return;
        }


    }
}
