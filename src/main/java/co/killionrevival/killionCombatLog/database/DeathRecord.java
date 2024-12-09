package co.killionrevival.killioncombatlog.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class DeathRecord {
    private final UUID playerUUID;
    private final String killerName;
    private final long deathTime;
    private final String location;

    public boolean isExpired(long maxAge) {
        return System.currentTimeMillis() - deathTime > maxAge * 1000;
    }
}