package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

/**
 * Handles death-related events in combat situations.
 */
@RequiredArgsConstructor
public class CombatDeathListener implements Listener {
    private final KillionCombatLog plugin;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (plugin.getCombatManager().isInCombat(victim)) {
            plugin.getCombatManager().handlePlayerDeath(victim, killer);

            if (plugin.getConfig().getBoolean("settings.custom-death-messages", true)) {
                String message = plugin.getConfig().getString(
                                "messages.combat-death",
                                "&c%victim% was slain by %killer% in combat!"
                        )
                        .replace("%victim%", victim.getName())
                        .replace("%killer%", killer != null ? killer.getName() : "unknown");

                event.setDeathMessage(MessageUtility.colorize(message));
            }
        }

        if (killer != null && plugin.getCombatManager().isInCombat(killer)) {
            String message = plugin.getConfig().getString(
                    "messages.combat-kill",
                    "&aYou are no longer in combat after defeating %victim%!"
            ).replace("%victim%", victim.getName());

            killer.sendMessage(MessageUtility.chatComponent(message));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLoginDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUniqueId();

        if (!plugin.getCombatLogManager().hasDeathRecord(playerUUID)) {
            return;
        }

        String killerName = plugin.getCombatLogManager().getKillerName(playerUUID);

        event.setDeathMessage(null);
        String message = plugin.getConfig().getString(
                "messages.offline-death",
                "&cYou were killed by &6%killer%&c while logged out."
        ).replace("%killer%", killerName != null ? killerName : "unknown");

        player.sendMessage(MessageUtility.chatComponent(message));
        plugin.getCombatLogManager().removeDeathRecord(playerUUID);
    }
}