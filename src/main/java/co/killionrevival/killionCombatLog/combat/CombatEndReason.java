package co.killionrevival.killioncombatlog.combat;

/**
 * Reasons why a combat session might end
 */
public enum CombatEndReason {
    NONE,                       // Combat hasn't ended
    TIMER_EXPIRED,             // No engagement within timer duration
    MAX_SESSION_LENGTH_EXCEEDED, // Total session length exceeded maximum
    COMBATANT_VICTORY,         // Combatant killed the victim
    VICTIM_VICTORY,            // Victim killed the combatant
    MUTUAL_DEATH,              // Both died simultaneously (edge case)
    ENTERED_SAFE_ZONE;         // Combat ended because player entered a safe zone

    /**
     * Returns a friendly message for why combat ended
     */
    public String getMessage() {
        return switch (this) {
            case NONE -> "Combat is ongoing";
            case TIMER_EXPIRED -> "Combat expired due to no engagement";
            case MAX_SESSION_LENGTH_EXCEEDED -> "Combat exceeded maximum duration";
            case COMBATANT_VICTORY -> "Combat ended - Combatant victorious";
            case VICTIM_VICTORY -> "Combat ended - Victim victorious";
            case MUTUAL_DEATH -> "Combat ended - Mutual defeat";
            case ENTERED_SAFE_ZONE -> "Combat ended - Entered safe zone";
        };
    }

    /**
     * Returns whether this is a victory condition
     */
    public boolean isVictoryCondition() {
        return this == COMBATANT_VICTORY ||
                this == VICTIM_VICTORY ||
                this == MUTUAL_DEATH;
    }
}