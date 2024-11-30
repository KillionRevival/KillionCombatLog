package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.events.PlayerCombatStateChangedEvent;
import co.killionrevival.killioncombatlog.logger.events.PlayerCombatLogEvent;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.RequiredArgsConstructor;
import net.citizensnpcs.api.npc.NPC;
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

        if (!plugin.getCombatManager().isPlayerDead(playerUUID)) {
            return;
        }

        String killerName = plugin.getCombatManager().getKillerName(playerUUID);
        if (killerName == null) {
            killerName = "unknown";
        }

        player.getInventory().clear();
        player.setHealth(0);

        String deathMessage = plugin.getConfig().getString(
                "messages.combat-log-death",
                "&cYou were killed by &6%killer%&c while logged out."
        ).replace("%killer%", killerName);

        player.sendMessage(MessageUtility.chatComponent(deathMessage));

        plugin.getCombatManager().setPlayerAsDead(playerUUID, null, false);
    }

    /**
     * Handles players quitting while in combat.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getServer().getOnlinePlayers().size() <= 1) {
            return;
        }

        if (!plugin.getCombatManager().isInCombat(player)) {
            return;
        }

        if (plugin.getConfig().getBoolean("settings.debug-mode", false)) {
            plugin.getLogger().info(String.format(
                    "Player %s attempted to combat log at location %s",
                    player.getName(),
                    player.getLocation().toString()
            ));
        }

        plugin.getServer().getPluginManager().callEvent(new PlayerCombatLogEvent(player));
    }

    /**
     * Handles players rejoining after combat logging.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCombatLoggerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!plugin.getCombatLogManager().isCombatLogger(playerUUID)) {
            return;
        }

        UUID npcUUID = plugin.getCombatLogManager().getPlayerNPC(playerUUID);
        if (npcUUID != null) {
            NPC npc = plugin.getNPCManager().getNPC(npcUUID);
            if (npc != null && npc.getEntity() instanceof Player npcPlayer) {
                player.setHealth(npcPlayer.getHealth());
                player.setFoodLevel(npcPlayer.getFoodLevel());
                player.setExp(npcPlayer.getExp());
                player.setLevel(npcPlayer.getLevel());
            }
        }

        plugin.getCombatLogManager().removePlayer(playerUUID);

        if (plugin.getCombatManager().isInCombat(player)) {
            plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(player, true)
            );

            String message = plugin.getConfig().getString(
                    "messages.still-in-combat",
                    "&cYou are still in combat!"
            );
            player.sendMessage(MessageUtility.chatComponent(message));
        }
    }
}