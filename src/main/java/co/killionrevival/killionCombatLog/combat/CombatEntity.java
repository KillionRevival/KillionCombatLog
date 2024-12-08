package co.killionrevival.killioncombatlog.combat;

import lombok.Getter;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents either a Player or Doppel in the combat system.
 * Manages combat sessions and state for the entity.
 */
public class CombatEntity {
    @Getter private final UUID entityId;
    @Getter private final Set<CombatSession> activeSessions = new HashSet<>();
    @Getter private Doppel doppel = null;
    private final boolean isDoppel;

    public CombatEntity(Player player) {
        this.entityId = player.getUniqueId();
        this.isDoppel = false;
    }

    public CombatEntity(Doppel doppel) {
        this.entityId = doppel.getOwnerId();
        this.isDoppel = true;
    }

    /**
     * Creates a Doppel for this entity when they log out in a PvP zone
     */
    public Doppel createDoppel(Player player) {
        if (!isDoppel && doppel == null) {
            doppel = new Doppel(player);

            // Transfer any active combat sessions to the Doppel
            if (!activeSessions.isEmpty()) {
                doppel.getEntity().getActiveSessions().addAll(activeSessions);
            }

            return doppel;
        }
        return null;
    }

    /**
     * Removes the Doppel for this entity
     */
    public void removeDoppel() {
        doppel = null;
    }

    /**
     * Checks if this entity already has an active session with the given entity
     */
    public boolean hasSessionWith(UUID otherId) {
        return activeSessions.stream()
                .anyMatch(session -> session.hasEntity(otherId));
    }

    /**
     * Adds a combat session to this entity if no session exists with the opponent
     * @return true if the session was added, false if a session already exists
     */
    public boolean addCombatSession(CombatSession session) {
        UUID opponentId = session.getOpponentId(entityId);
        if (opponentId == null || hasSessionWith(opponentId)) {
            return false;
        }

        activeSessions.add(session);

        // If we have a Doppel, add the session to it as well
        if (doppel != null) {
            doppel.getEntity().addCombatSession(session);
        }

        return true;
    }

    /**
     * Gets the existing combat session with a specific entity, if one exists
     */
    public CombatSession getSessionWith(UUID otherId) {
        return activeSessions.stream()
                .filter(session -> session.hasEntity(otherId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes a combat session from this entity
     */
    public void removeCombatSession(CombatSession session) {
        activeSessions.remove(session);

        // If we have a Doppel, remove the session from it as well
        if (doppel != null) {
            doppel.getEntity().removeCombatSession(session);
        }
    }

    /**
     * Handles entry into a safe zone by ending all combat sessions
     */
    public void handleSafeZoneEntry() {
        // End all combat sessions this entity is part of
        for (CombatSession session : new HashSet<>(activeSessions)) {
            session.forceEnd(CombatEndReason.ENTERED_SAFE_ZONE);
            activeSessions.remove(session);
        }
    }

    /**
     * Checks if the entity is in combat
     */
    public boolean isInCombat() {
        return !activeSessions.isEmpty();
    }

    /**
     * Gets whether this is a Doppel entity
     */
    public boolean isDoppel() {
        return isDoppel;
    }

    /**
     * Gets whether this entity has a Doppel
     */
    public boolean hasDoppel() {
        return doppel != null;
    }
}