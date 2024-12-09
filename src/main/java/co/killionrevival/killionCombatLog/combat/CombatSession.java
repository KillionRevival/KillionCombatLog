package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.util.LogUtil;
import lombok.Getter;

import java.util.UUID;

public class CombatSession {
    @Getter private final UUID combatantId;
    @Getter private final UUID victimId;
    @Getter private int remainingSeconds;
    @Getter private long totalSessionTime;
    private final long startTime;

    private final int maxDuration;
    private final int initialDuration;
    private final int reengagementDuration;
    private final int doppelSwapDuration;
    private final long maxSessionLength;

    public CombatSession(CombatEntity combatant,
                         CombatEntity victim,
                         int initialDuration,
                         int reengagementDuration,
                         int doppelSwapDuration,
                         int maxDuration,
                         long maxSessionLength) {
        if (combatant.getPlayerId().equals(victim.getPlayerId())) {
            throw new IllegalArgumentException("Cannot create combat session with self");
        }

        this.combatantId = combatant.getPlayerId();
        this.victimId = victim.getPlayerId();
        this.initialDuration = initialDuration;
        this.reengagementDuration = reengagementDuration;
        this.doppelSwapDuration = doppelSwapDuration;
        this.maxDuration = maxDuration;
        this.maxSessionLength = maxSessionLength;
        this.remainingSeconds = initialDuration;
        this.startTime = System.currentTimeMillis();
        this.totalSessionTime = 0;

        LogUtil.debug(String.format("Created combat session: initial=%d, reengagement=%d, doppelSwap=%d, max=%d",
                initialDuration, reengagementDuration, doppelSwapDuration, maxDuration));
    }

    public boolean handleReengagement(UUID entityId) {
        if (!hasEntity(entityId)) {
            return false;
        }
        int previousTime = remainingSeconds;
        this.remainingSeconds = Math.min(remainingSeconds + reengagementDuration, maxDuration);
        LogUtil.debug(String.format("Reengagement: %d -> %d seconds", previousTime, remainingSeconds));
        return true;
    }

    public boolean handleDoppelSwap(UUID originalId) {
        if (!hasEntity(originalId)) {
            return false;
        }
        int previousTime = remainingSeconds;
        this.remainingSeconds = Math.min(remainingSeconds + doppelSwapDuration, maxDuration);
        LogUtil.debug(String.format("Doppel swap: %d -> %d seconds", previousTime, remainingSeconds));
        return true;
    }

    public CombatEndReason handleDeath(UUID deadEntityId) {
        if (deadEntityId.equals(victimId)) {
            return CombatEndReason.COMBATANT_VICTORY;
        }
        if (deadEntityId.equals(combatantId)) {
            return CombatEndReason.VICTIM_VICTORY;
        }
        return CombatEndReason.NONE;
    }

    public CombatEndReason tick() {
        totalSessionTime = System.currentTimeMillis() - startTime;
        LogUtil.debug(String.format("Session tick - Remaining: %d, Total: %d", remainingSeconds, totalSessionTime/1000));

        if ((totalSessionTime / 1000) >= maxSessionLength) {
            LogUtil.debug("Session exceeded max length");
            return CombatEndReason.MAX_SESSION_LENGTH_EXCEEDED;
        }

        if (remainingSeconds > 0) {
            remainingSeconds--;
        }

        if (remainingSeconds <= 0) {
            LogUtil.debug("Session timer expired");
            return CombatEndReason.TIMER_EXPIRED;
        }

        return CombatEndReason.NONE;
    }

    public boolean hasEntity(UUID entityId) {
        return combatantId.equals(entityId) || victimId.equals(entityId);
    }

    public UUID getOpponentId(UUID entityId) {
        if (combatantId.equals(entityId)) return victimId;
        if (victimId.equals(entityId)) return combatantId;
        return null;
    }

    public void forceEnd(CombatEndReason reason) {
        LogUtil.debug(String.format("Force ending session with reason: %s", reason));
        this.remainingSeconds = 0;
    }
}
