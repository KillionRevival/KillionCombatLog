package co.killionrevival.killioncombatlog.combat.rules;

import org.bukkit.GameMode;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.Set;

/**
 * Rule that handles projectile-based combat interactions.
 */
public class ProjectileCombatRule implements ICombatRule<ProjectileHitEvent> {

    @Override
    public CombatResult evaluateInteraction(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        // Check if shooter is a player
        if (!(projectile.getShooter() instanceof Player shooter)) {
            return CombatResult.noCombat("Shooter is not a player");
        }

        // Check if hit entity is a player
        if (!(event.getHitEntity() instanceof Player victim)) {
            return CombatResult.noCombat("Target is not a player");
        }

        // Ignore non-damaging projectiles
        if (projectile instanceof Snowball || projectile instanceof Egg) {
            return CombatResult.noCombat("Non-damaging projectile type: " + projectile.getType());
        }

        // Check game modes
        if (shooter.getGameMode() == GameMode.CREATIVE ||
                shooter.getGameMode() == GameMode.SPECTATOR) {
            return CombatResult.noCombat("Shooter is in " + shooter.getGameMode() + " mode");
        }

        if (victim.getGameMode() == GameMode.CREATIVE ||
                victim.getGameMode() == GameMode.SPECTATOR) {
            return CombatResult.noCombat("Victim is in " + victim.getGameMode() + " mode");
        }

        return CombatResult.combat(Set.of(shooter, victim),
                "Valid projectile combat interaction: " + projectile.getType());
    }

    @Override
    public Class<ProjectileHitEvent> getEventType() {
        return ProjectileHitEvent.class;
    }
}