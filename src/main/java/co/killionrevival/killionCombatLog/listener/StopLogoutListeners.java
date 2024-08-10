package co.killionrevival.killionCombatLog.listener;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killionCombatLog.managers.LogoutManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class StopLogoutListeners implements Listener {
    private LogoutManager manager;

    public StopLogoutListeners() {
        this.manager = KillionCombatLog.getInstance()
                                       .getLogoutManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!manager.isLoggingOut(event.getPlayer().getUniqueId())) {
            return;
        }

        // Cancel logout
        manager.cancelLogout(event.getPlayer());
    }
}
