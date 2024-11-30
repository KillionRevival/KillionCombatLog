package co.killionrevival.killioncombatlog.combat.persistence.repository;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable data class representing a player's death record.
 */
@Value
@Builder
public class DeathRecord {
    UUID playerUUID;
    String killerName;
    String location;
    Instant timestamp;
}