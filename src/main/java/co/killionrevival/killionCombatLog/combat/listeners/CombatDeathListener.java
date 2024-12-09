package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@RequiredArgsConstructor
public class CombatDeathListener implements Listener {
    private final KillionCombatLog plugin;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (plugin.getCombatManager().isInCombat(victim)) {
            plugin.getCombatManager().handlePlayerDeath(victim, killer);

            if (plugin.getConfigManager().isCustomDeathMessages()) {
                String message = plugin.getConfigManager().getCombatDeathMessage()
                        .replace("%victim%", victim.getName())
                        .replace("%killer%", killer != null ? killer.getName() : "unknown");
                event.setDeathMessage(MessageUtility.colorize(message));
            }
        }

        if (killer != null && plugin.getCombatManager().isInCombat(killer)) {
            String message = plugin.getConfigManager().getCombatKillMessage()
                    .replace("%victim%", victim.getName());
            killer.sendMessage(MessageUtility.chatComponent(message));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLoginDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!plugin.getCombatLogManager().hasDeathRecord(player.getUniqueId())) {
            return;
        }

        String killerName = plugin.getCombatLogManager().getKillerName(player.getUniqueId());

        event.setDeathMessage(null);
        String message = plugin.getConfigManager().getOfflineDeathMessage()
                .replace("%killer%", killerName != null ? killerName : "unknown");

        player.sendMessage(MessageUtility.chatComponent(message));
        plugin.getCombatLogManager().removeDeathRecord(player.getUniqueId());
    }
}
