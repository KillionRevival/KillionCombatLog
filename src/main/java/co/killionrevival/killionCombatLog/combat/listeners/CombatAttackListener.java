package co.killionrevival.killioncombatlog.combat.listeners;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import co.killionrevival.killioncombatlog.util.WorldGuardHelper;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Handles combat initiation through attacks
 */
@RequiredArgsConstructor
public class CombatAttackListener implements Listener {
    private final KillionCombatLog plugin;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Player) {
                attacker = (Player) shooter;
            }
        }

        if (attacker == null || attacker == victim) {
            return;
        }

        if (WorldGuardHelper.isPvPEnabled(victim.getLocation())) {
            LogUtil.debug(String.format("Initiating combat between %s and %s",
                    attacker.getName(), victim.getName()));
            plugin.getCombatManager().initiateCombat(attacker, victim);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof Player victim)) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player attacker)) {
            return;
        }

        if (attacker == victim) {
            return;
        }

        if (WorldGuardHelper.isPvPEnabled(victim.getLocation())) {
            LogUtil.debug(String.format("Initiating combat from projectile between %s and %s",
                    attacker.getName(), victim.getName()));
            plugin.getCombatManager().initiateCombat(attacker, victim);
        }
    }
}