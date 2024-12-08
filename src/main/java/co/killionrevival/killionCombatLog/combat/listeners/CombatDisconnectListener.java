package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.CombatEntity;
import co.killionrevival.killioncombatlog.combat.Doppel;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import co.killionrevival.killioncombatlog.logger.events.PlayerCombatLogEvent;
import co.killionrevival.killioncombatlog.util.LogUtil;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import co.killionrevival.killioncombatlog.util.WorldGuardHelper;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Handles combat logging related events including player disconnects and reconnects.
 */
@RequiredArgsConstructor
public class CombatDisconnectListener implements Listener {

    private final KillionCombatLog plugin;

    /**
     * Handles players attempting to rejoin after dying during combat log.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onAfterDeathJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!plugin.getCombatLogManager().hasDeathRecord(playerUUID)) {
            return;
        }

        LogUtil.debug("Player " + player.getName() + " joining after combat log death");

        String killerName = plugin.getCombatLogManager().getKillerName(playerUUID);
        if (killerName == null) {
            killerName = "unknown";
        }

        // Execute death handling on next tick to ensure proper game state
        new BukkitRunnable() {
            @Override
            public void run() {
                // Only process if player is still online
                if (!player.isOnline()) return;

                // Clear inventory and kill player if they're in survival mode
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    player.getInventory().clear();
                    player.setHealth(0);
                }

                String deathMessage = plugin.getConfig().getString(
                        "messages.combat-log-death",
                        "&cYou were killed by &6%killer%&c while logged out."
                ).replace("%killer%", killerName);

                player.sendMessage(MessageUtility.chatComponent(deathMessage));
                plugin.getCombatLogManager().removeDeathRecord(playerUUID);
            }
        }.runTask(plugin);
    }

    /**
     * Handles players quitting while in combat or in PvP regions.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Skip if player has bypass permission
        if (player.hasPermission("killioncombatlog.bypass")) {
            LogUtil.debug("Player " + player.getName() + " has combat log bypass permission");
            return;
        }

        // Only create Doppel if in PvP region
        if (!WorldGuardHelper.isPvPEnabled(player.getLocation())) {
            LogUtil.debug("Player " + player.getName() + " quit in safe zone - no Doppel created");
            return;
        }

        // Check if server is basically empty (configurable minimum)
        int minPlayers = plugin.getConfig().getInt("settings.minimum-online-players", 1);
        if (plugin.getServer().getOnlinePlayers().size() <= minPlayers) {
            LogUtil.debug("Server below minimum player threshold - no Doppel created");
            return;
        }

        // Check if player is in creative or spectator mode
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            LogUtil.debug("Player " + player.getName() + " in creative/spectator mode - no Doppel created");
            return;
        }

        CombatEntity entity = plugin.getEntityManager().getEntity(player);

        // Create Doppel if in combat or if configured to create for all PvP zone disconnects
        boolean createOnlyInCombat = plugin.getConfig().getBoolean("settings.doppel-only-in-combat", true);
        if (createOnlyInCombat && !entity.isInCombat()) {
            LogUtil.debug("Player " + player.getName() + " not in combat - no Doppel created");
            return;
        }

        // Fire combat log event which will trigger Doppel creation
        LogUtil.debug("Creating Doppel for disconnected player: " + player.getName());
        plugin.getServer().getPluginManager().callEvent(new PlayerCombatLogEvent(player));

        // Set custom quit message if configured
        if (plugin.getConfig().getBoolean("settings.custom-quit-messages", true)) {
            String quitMessage = plugin.getConfig().getString(
                    "messages.combat-quit",
                    "&c%player% has combat logged!"
            ).replace("%player%", player.getName());

            event.setQuitMessage(MessageUtility.colorize(quitMessage));
        }
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

        LogUtil.debug("Player " + player.getName() + " rejoining with active Doppel");

        // Get the Doppel and transfer its state
        Doppel doppel = entity.getDoppel();
        if (doppel != null) {
            // Transfer health and inventory state
            doppel.transferToPlayer(player);

            // Handle combat state
            if (entity.isInCombat()) {
                // Fire state change event
                plugin.getServer().getPluginManager().callEvent(
                        new PlayerCombatStateChangedEvent(player, true)
                );

                // Get highest remaining time from any active sessions
                int highestRemainingTime = entity.getActiveSessions().stream()
                        .mapToInt(session -> session.getRemainingSeconds())
                        .max()
                        .orElse(0);

                // Send combat status message
                String message = plugin.getConfig().getString(
                        "messages.still-in-combat",
                        "&cYou are still in combat for %seconds% more seconds!"
                ).replace("%seconds%", String.valueOf(highestRemainingTime));

                player.sendMessage(MessageUtility.chatComponent(message));

                // Apply combat penalties if configured
                if (plugin.getConfig().getBoolean("settings.rejoin-penalties.enabled", true)) {
                    applyRejoinPenalties(player);
                }
            }

            // Remove the Doppel
            entity.removeDoppel();
            LogUtil.debug("Removed Doppel for " + player.getName() + " after successful rejoin");
        }
    }

    /**
     * Applies configured penalties for rejoining during combat.
     */
    private void applyRejoinPenalties(Player player) {
        // Apply configured health reduction
        double healthPenalty = plugin.getConfig().getDouble("settings.rejoin-penalties.health-percent", 0.0);
        if (healthPenalty > 0) {
            double maxHealth = player.getMaxHealth();
            double newHealth = maxHealth * (1 - (healthPenalty / 100));
            player.setHealth(Math.max(0.5, newHealth)); // Ensure at least half a heart
        }

        // Apply configured effects/other penalties here
    }
}