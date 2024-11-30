package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.combat.rules.CombatResult;
import co.killionrevival.killioncombatlog.util.LogUtil;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * Handles combat state entry events through direct damage and projectiles.
 */
@RequiredArgsConstructor
public class CombatStateListener implements Listener {

    private final KillionCombatLog plugin;

    /**
     * Handles direct player vs player combat interactions.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        LogUtil.debug("Processing EntityDamageByEntityEvent for combat evaluation");
        CombatResult result = plugin.getCombatManager().getRuleEngine().evaluateEvent(event);

        if (result.isShouldEnterCombat()) {
            for (Player combatant : result.getCombatants()) {
                plugin.getCombatManager().addPlayer(combatant);
                LogUtil.combat(String.format("Player %s entered combat due to direct damage", combatant.getName()));

                String message = plugin.getConfig().getString(
                        "messages.combat-engaged",
                        "&cYou have entered combat!"
                );
                combatant.sendMessage(MessageUtility.chatComponent(message));
            }
        } else {
            LogUtil.debug("Combat not initiated: " + result.getReason());
        }
    }

    /**
     * Handles projectile-based combat interactions.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        LogUtil.debug("Processing ProjectileHitEvent for combat evaluation");
        CombatResult result = plugin.getCombatManager().getRuleEngine().evaluateEvent(event);

        if (result.isShouldEnterCombat()) {
            for (Player combatant : result.getCombatants()) {
                plugin.getCombatManager().addPlayer(combatant);
                LogUtil.combat(String.format("Player %s entered combat due to projectile combat", combatant.getName()));

                String message = plugin.getConfig().getString(
                        "messages.combat-engaged-projectile",
                        "&cYou have entered combat due to projectile combat!"
                );
                combatant.sendMessage(MessageUtility.chatComponent(message));
            }
        } else {
            LogUtil.debug("Projectile combat not initiated: " + result.getReason());
        }
    }
}