package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.CombatEntity;
import co.killionrevival.killioncombatlog.combat.Doppel;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import co.killionrevival.killioncombatlog.logger.events.PlayerCombatLogEvent;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import co.killionrevival.killioncombatlog.util.WorldGuardHelper;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Handles combat logging related events.
 */
@RequiredArgsConstructor
public class CombatDisconnectListener implements Listener {

    private final KillionCombatLog plugin;

    /**
     * Handles players attempting to join after dying during combat log.
     */
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

        // Clear inventory and kill player
        player.getInventory().clear();
        player.setHealth(0);

        String deathMessage = plugin.getConfig().getString(
                "messages.combat-log-death",
                "&cYou were killed by &6%killer%&c while logged out."
        ).replace("%killer%", killerName);

        player.sendMessage(MessageUtility.chatComponent(deathMessage));
        plugin.getCombatLogManager().removeDeathRecord(playerUUID);
    }

    /**
     * Handles players quitting while in combat or in PvP regions.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Only create Doppel if in PvP region
        if (!WorldGuardHelper.isPvPEnabled(player.getLocation())) {
            return;
        }

        // Check if server is basically empty
        if (plugin.getServer().getOnlinePlayers().size() <= 1) {
            return;
        }

        CombatEntity entity = plugin.getEntityManager().getEntity(player);

        // Create Doppel regardless of combat state if in PvP region
        plugin.getServer().getPluginManager().callEvent(new PlayerCombatLogEvent(player));
    }

    /**
     * Handles players rejoining when they have a Doppel.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCombatLoggerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        CombatEntity entity = plugin.getEntityManager().getEntity(player);

        if (!entity.hasDoppel()) {
            return;
        }

        // Get the Doppel and transfer its state
        Doppel doppel = entity.getDoppel();
        if (doppel != null) {
            // Transfer health state
            doppel.transferToPlayer(player);

            // Combat sessions are already handled by CombatEntity

            if (entity.isInCombat()) {
                plugin.getServer().getPluginManager().callEvent(
                        new PlayerCombatStateChangedEvent(player, true)
                );

                // Get remaining time from any active sessions
                int highestRemainingTime = entity.getActiveSessions().stream()
                        .mapToInt(session -> session.getRemainingSeconds())
                        .max()
                        .orElse(0);

                String message = plugin.getConfig().getString(
                        "messages.still-in-combat",
                        "&cYou are still in combat for %seconds% more seconds!"
                ).replace("%seconds%", String.valueOf(highestRemainingTime));

                player.sendMessage(MessageUtility.chatComponent(message));
            }

            // Remove the Doppel
            entity.removeDoppel();
        }
    }

    /**
     * Checks if PvP is enabled at the given location
     */
    private boolean isPvPEnabled(Location location) {
        // TODO: Implement WorldGuard check
        return true;
    }
}