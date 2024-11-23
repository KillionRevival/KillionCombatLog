package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.logger.events.PlayerCombatLogEvent;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Listener for player-related events to manage combat states and handle combat logging.
 */
public class CombatListener implements Listener {

    private final KillionCombatLog plugin;
    private final Map<UUID, BukkitRunnable> countdownTasks = new HashMap<>();

    /**
     * Constructor to initialize the listener with the main plugin instance.
     *
     * @param plugin The main plugin instance.
     */
    public CombatListener(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the scenario where a player joins after dying due to combat logging.
     * Ensures that they experience the consequences appropriately.
     *
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onAfterDeathJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player is marked as dead due to combat logging
        if (!plugin.getCombatManager().isPlayerDead(player.getUniqueId())) {
            return;
        }

        // Clear inventory and set health to zero
        player.getInventory().clear();
        player.setHealth(0);
    }

    /**
     * Handles players who quit the game while in combat.
     * Triggers the combat logging mechanism.
     *
     * @param event The PlayerQuitEvent.
     */
    @EventHandler
    public void onCombatQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // If no other players are online, do nothing
        if (plugin.getServer().getOnlinePlayers().isEmpty()) {
            return;
        }

        // If the player is not in combat, do nothing
        if (!plugin.getCombatManager().isInCombat(player)) {
            return;
        }

        // Trigger the PlayerCombatLogEvent
        plugin.getServer().getPluginManager().callEvent(new PlayerCombatLogEvent(player));
    }

    /**
     * Handles players rejoining the game after combat logging.
     * Restores their state if their NPC is still alive.
     *
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onCombatLoggerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player is a combat logger
        if (!plugin.getCombatLogManager().isCombatLogger(player.getUniqueId())) {
            return;
        }

        UUID npcUniqueId = plugin.getCombatLogManager().getPlayerNPC(player.getUniqueId());

        if (npcUniqueId != null) {
            NPC npc = plugin.getNPCManager().getNPC(npcUniqueId);

            if (npc != null && npc.getEntity() instanceof Player npcPlayer) {

                // Synchronize health with the NPC
                player.setHealth(npcPlayer.getHealth());
            }
        }

        // Remove the NPC and update combat state
        plugin.getCombatLogManager().removePlayer(player.getUniqueId());

        if (countdownTasks.containsKey(player.getUniqueId())) {
            plugin.getServer().getPluginManager().callEvent(new PlayerCombatStateChangedEvent(player, true));
        }
    }

    /**
     * Handles players who were killed while offline due to their NPC being defeated.
     * Applies the death consequences on login.
     *
     * @param event The PlayerDeathEvent.
     */
    @EventHandler
    public void onLoginDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Check if the player was marked as dead
        if (!plugin.getCombatManager().isPlayerDead(player.getUniqueId())) {
            return;
        }

        String killerName = plugin.getCombatManager().getKillerName(player.getUniqueId());

        event.setDeathMessage(null);
        player.sendMessage(MessageUtility.chatComponent("&cYou were killed by &6" + killerName + "&c while logged out."));
        plugin.getCombatManager().setTimeRemain(player, 0);
        plugin.getCombatManager().setPlayerAsDead(player.getUniqueId(), null, false);
    }

    /**
     * Removes players from combat upon death.
     *
     * @param event The PlayerDeathEvent.
     */
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (plugin.getCombatManager().isInCombat(player)) {
            plugin.getCombatManager().removePlayer(player);
        }

        if (killer != null && plugin.getCombatManager().isInCombat(killer)) {
            plugin.getCombatManager().removePlayer(killer);
        }
    }

    /**
     * Adds players to combat when they damage each other directly.
     *
     * @param event The EntityDamageByEntityEvent.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player damager)) {
            return;
        }

        // Ignore if either player is in creative or spectator mode
        if (damager.getGameMode() == GameMode.CREATIVE || victim.getGameMode() == GameMode.CREATIVE ||
                victim.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Add both players to combat
        plugin.getCombatManager().addPlayer(victim);
        plugin.getCombatManager().addPlayer(damager);
    }

    /**
     * Adds players to combat when they hit each other with projectiles.
     *
     * @param event The ProjectileHitEvent.
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (!(projectile.getShooter() instanceof Player shooter)) {
            return;
        }

        // Ignore non-damaging projectiles
        if (projectile instanceof Snowball || projectile instanceof Egg) {
            return;
        }

        if (!(event.getHitEntity() instanceof Player victim)) {
            return;
        }

        // Ignore if either player is in creative or spectator mode
        if (shooter.getGameMode() == GameMode.CREATIVE || victim.getGameMode() == GameMode.CREATIVE ||
                victim.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Add both players to combat
        plugin.getCombatManager().addPlayer(shooter);
        plugin.getCombatManager().addPlayer(victim);
    }

    /**
     * Updates the action bar messages and countdown timers when a player's combat state changes.
     *
     * @param event The PlayerCombatStateChangedEvent.
     */
    @EventHandler
    public void onCombatStateChanged(PlayerCombatStateChangedEvent event) {
        Player player = event.getPlayer();
        boolean inCombat = event.isInCombat();

        if (player == null) {
            return;
        }

        // Handle exiting combat
        if (!inCombat) {
            BukkitRunnable existingTask = this.countdownTasks.remove(player.getUniqueId());
            if (existingTask != null) {
                existingTask.cancel();
            }
            player.sendActionBar(MessageUtility.chatComponent(plugin.getConfig().getString("messages.no-longer")));
            return;
        }

        // Handle entering combat
        BukkitRunnable existingTask = this.countdownTasks.get(player.getUniqueId());
        if (existingTask != null) {
            existingTask.cancel();
            this.countdownTasks.remove(player.getUniqueId());
        }

        // Create a new countdown task
        BukkitRunnable countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getCombatManager().isInCombat(player)) {
                    countdownTasks.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                int seconds = plugin.getCombatManager().getTimeRemain(player);

                plugin.getCombatManager().decreaseTimeRemain(player);
                if (seconds <= 0) {
                    plugin.getCombatManager().removePlayer(player);
                    countdownTasks.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                String message = plugin.getConfig().getString("messages.in-combat");
                if (message != null) {
                    message = message.replace("%seconds%", String.valueOf(seconds));
                    player.sendActionBar(MessageUtility.chatComponent(message));
                }
            }
        };

        this.countdownTasks.put(player.getUniqueId(), countdownTask);
        countdownTask.runTaskTimer(plugin, 0, 20L);
    }
}
