package co.killionrevival.killioncombatlog.combat;

/**
 * Reasons why a combat session might end.
 */
public enum CombatEndReason {
    NONE,
    TIMER_EXPIRED,
    MAX_SESSION_LENGTH_EXCEEDED,
    COMBATANT_VICTORY,
    VICTIM_VICTORY,
    MUTUAL_DEATH,
    ENTERED_SAFE_ZONE,
    PLUGIN_SHUTDOWN;

    public String getMessage() {
        return switch (this) {
            case NONE -> "Combat is ongoing";
            case TIMER_EXPIRED -> "Combat expired due to no engagement";
            case MAX_SESSION_LENGTH_EXCEEDED -> "Combat exceeded maximum duration";
            case COMBATANT_VICTORY -> "Combat ended - Combatant victorious";
            case VICTIM_VICTORY -> "Combat ended - Victim victorious";
            case MUTUAL_DEATH -> "Combat ended - Mutual defeat";
            case ENTERED_SAFE_ZONE -> "Combat ended - Entered safe zone";
            case PLUGIN_SHUTDOWN -> "Combat ended - Plugin shutdown";
        };
    }

    public boolean isVictoryCondition() {
        return this == COMBATANT_VICTORY ||
                this == VICTIM_VICTORY ||
                this == MUTUAL_DEATH;
    }
}
