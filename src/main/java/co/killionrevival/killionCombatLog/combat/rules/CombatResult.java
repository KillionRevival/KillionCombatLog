package co.killionrevival.killioncombatlog.combat.rules;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the result of a combat rule evaluation.
 * Contains information about whether combat should be initiated and which players are involved.
 */
@Getter
public class CombatResult {
    private final boolean shouldEnterCombat;
    private final Set<Player> combatants;
    private final String reason;

    private CombatResult(boolean shouldEnterCombat, Set<Player> combatants, String reason) {
        this.shouldEnterCombat = shouldEnterCombat;
        this.combatants = Collections.unmodifiableSet(new HashSet<>(combatants));
        this.reason = reason;
    }

    /**
     * Creates a positive combat result with the specified combatants.
     */
    public static CombatResult combat(Set<Player> combatants, String reason) {
        return new CombatResult(true, combatants, reason);
    }

    /**
     * Creates a negative combat result with the specified reason.
     */
    public static CombatResult noCombat(String reason) {
        return new CombatResult(false, Collections.emptySet(), reason);
    }
}