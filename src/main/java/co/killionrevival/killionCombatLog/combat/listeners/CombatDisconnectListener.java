package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.CombatEntity;
import co.killionrevival.killioncombatlog.logger.PlayerCombatLogEvent;
import co.killionrevival.killioncombatlog.util.WorldGuardHelper;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class CombatDisconnectListener implements Listener {
    private final KillionCombatLog plugin;

    @EventHandler(priority = EventPriority.HIGH)
    public void onAfterDeathJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!plugin.getCombatLogManager().hasDeathRecord(playerUUID)) {
            return;
        }

        String killerName = plugin.getCombatLogManager().getKillerName(playerUUID);
        if (killerName == null) {
            killerName = "unknown";
        }

        player.getInventory().clear();
        player.setHealth(0);

        String deathMessage = plugin.getConfigManager().getCombatLogDeathMessage()
                .replace("%killer%", killerName);

        player.sendMessage(co.killionrevival.killioncombatlog.util.MessageUtility.chatComponent(deathMessage));
        plugin.getCombatLogManager().removeDeathRecord(playerUUID);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CombatEntity entity = plugin.getEntityManager().getEntity(player);
        if (entity == null) return;

        boolean isPvPZone = WorldGuardHelper.isPvPEnabled(player.getLocation());

        // If player is in combat and leaves in PvP zone, call event
        if (entity.isInCombat() && isPvPZone && plugin.getServer().getOnlinePlayers().size() > 1) {
            plugin.getServer().getPluginManager().callEvent(new PlayerCombatLogEvent(player));
        } else {
            // If not in pvp zone or not in combat, just handle logout (no Doppel)
            entity.handleLogout(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCombatLoggerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CombatEntity entity = plugin.getEntityManager().getEntity(player);
        if (entity == null) return;

        if (entity.hasDoppel()) {
            // Player logged back in while Doppel was active
            entity.handleLoginWithDoppel(player);
        }
    }
}
