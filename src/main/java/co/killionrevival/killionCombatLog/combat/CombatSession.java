package co.killionrevival.killioncombatlog.combat;

import lombok.Getter;
import java.util.UUID;

/**
 * Represents a combat session between exactly two entities.
 * A session is strictly 1v1, representing a fight between a combatant (attacker)
 * and a victim (defender).
 */
public class CombatSession {
    @Getter private final UUID combatantId;  // The attacker/initiator
    @Getter private final UUID victimId;     // The defender/target
    @Getter private int remainingSeconds;
    @Getter private long totalSessionTime;
    private final long startTime;

    private final int maxDuration;
    private final int initialDuration;
    private final int reengagementDuration;
    private final int doppelSwapDuration;
    private final long maxSessionLength;

    /**
     * Creates a new 1v1 combat session between two entities.
     * @throws IllegalArgumentException if combatant and victim are the same entity
     */
    public CombatSession(CombatEntity combatant,
                         CombatEntity victim,
                         int initialDuration,
                         int reengagementDuration,
                         int doppelSwapDuration,
                         int maxDuration,
                         long maxSessionLength) {
        if (combatant.getEntityId().equals(victim.getEntityId())) {
            throw new IllegalArgumentException("Cannot create combat session with self");
        }

        this.combatantId = combatant.getEntityId();
        this.victimId = victim.getEntityId();
        this.initialDuration = initialDuration;
        this.reengagementDuration = reengagementDuration;
        this.doppelSwapDuration = doppelSwapDuration;
        this.maxDuration = maxDuration;
        this.maxSessionLength = maxSessionLength;
        this.remainingSeconds = initialDuration;
        this.startTime = System.currentTimeMillis();
        this.totalSessionTime = 0;
    }

    /**
     * Called when the original pair re-engages in combat
     */
    public boolean handleReengagement(UUID entityId) {
        if (!hasEntity(entityId)) {
            return false;
        }
        this.remainingSeconds = Math.min(remainingSeconds + reengagementDuration, maxDuration);
        return true;
    }

    /**
     * Called when a Doppel swaps in for one of the original entities
     */
    public boolean handleDoppelSwap(UUID originalId) {
        if (!hasEntity(originalId)) {
            return false;
        }
        this.remainingSeconds = Math.min(remainingSeconds + doppelSwapDuration, maxDuration);
        return true;
    }

    /**
     * Handles an entity death and determines if it ends combat
     */
    public CombatEndReason handleDeath(UUID deadEntityId) {
        if (deadEntityId.equals(victimId)) {
            return CombatEndReason.COMBATANT_VICTORY;
        }
        if (deadEntityId.equals(combatantId)) {
            return CombatEndReason.VICTIM_VICTORY;
        }
        return CombatEndReason.NONE;
    }

    /**
     * Updates the timer and checks if combat should end
     */
    public CombatEndReason tick() {
        totalSessionTime = System.currentTimeMillis() - startTime;

        if (totalSessionTime / 1000 >= maxSessionLength) {
            return CombatEndReason.MAX_SESSION_LENGTH_EXCEEDED;
        }

        if (remainingSeconds > 0) {
            remainingSeconds--;
        }

        if (remainingSeconds <= 0) {
            return CombatEndReason.TIMER_EXPIRED;
        }

        return CombatEndReason.NONE;
    }

    /**
     * Gets whether an entity is part of this session
     */
    public boolean hasEntity(UUID entityId) {
        return combatantId.equals(entityId) || victimId.equals(entityId);
    }

    /**
     * Gets the opponent's ID for the given entity
     */
    public UUID getOpponentId(UUID entityId) {
        if (combatantId.equals(entityId)) return victimId;
        if (victimId.equals(entityId)) return combatantId;
        return null;
    }

    /**
     * Force sets the remaining time to a specific value
     */
    public void setRemainingTime(int seconds) {
        this.remainingSeconds = Math.min(seconds, maxDuration);
    }

    /**
     * Forces the combat session to end immediately
     */
    public void forceEnd(CombatEndReason reason) {
        this.remainingSeconds = 0;
    }
}