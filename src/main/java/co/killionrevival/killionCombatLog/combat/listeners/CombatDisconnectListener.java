package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.CombatEntity;
import co.killionrevival.killioncombatlog.logger.PlayerCombatLogEvent;
import co.killionrevival.killioncombatlog.util.LogUtil;
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

        LogUtil.debug(String.format("Player %s quit. PvP Zone: %b, In Combat: %b",
            player.getName(), isPvPZone, entity.isInCombat()));

        if (isPvPZone) {
            LogUtil.debug("Processing PvP zone logout for " + player.getName());
            entity.handleLogout(true);

            if (entity.isInCombat() && plugin.getServer().getOnlinePlayers().size() > 1) {
                plugin.getServer().getPluginManager().callEvent(new PlayerCombatLogEvent(player));
            }
        } else {
            LogUtil.debug("Processing non-PvP zone logout for " + player.getName());
            entity.handleLogout(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCombatLoggerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CombatEntity entity = plugin.getEntityManager().getEntity(player);
        if (entity == null) return;

        if (entity.hasDoppel()) {
            LogUtil.debug("Player " + player.getName() + " rejoined with active Doppel");
            entity.handleLoginWithDoppel(player);
        }
    }
}
