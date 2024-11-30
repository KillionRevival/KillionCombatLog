package co.killionrevival.killioncombatlog.combat.rules;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Set;

/**
 * Rule that checks if players are in valid game modes for combat.
 */
public class GameModeRule implements ICombatRule<EntityDamageByEntityEvent> {

    @Override
    public CombatResult evaluateInteraction(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) ||
                !(event.getDamager() instanceof Player attacker)) {
            return CombatResult.noCombat("Not a player vs player interaction");
        }

        // Check attacker game mode
        if (attacker.getGameMode() == GameMode.CREATIVE ||
                attacker.getGameMode() == GameMode.SPECTATOR) {
            return CombatResult.noCombat("Attacker is in " + attacker.getGameMode() + " mode");
        }

        // Check victim game mode
        if (victim.getGameMode() == GameMode.CREATIVE ||
                victim.getGameMode() == GameMode.SPECTATOR) {
            return CombatResult.noCombat("Victim is in " + victim.getGameMode() + " mode");
        }

        return CombatResult.combat(Set.of(attacker, victim), "Valid PvP interaction");
    }

    @Override
    public Class<EntityDamageByEntityEvent> getEventType() {
        return EntityDamageByEntityEvent.class;
    }
}