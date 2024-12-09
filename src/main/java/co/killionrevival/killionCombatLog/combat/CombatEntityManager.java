package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatEntityManager {
    private final ConcurrentHashMap<UUID, CombatEntity> entities = new ConcurrentHashMap<>();
    private final KillionCombatLog plugin;

    public CombatEntityManager(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    public CombatEntity getEntity(Player player) {
        return entities.computeIfAbsent(player.getUniqueId(), id -> {
            LogUtil.debug("Creating new CombatEntity for player: " + player.getName());
            return new CombatEntity(plugin, player);
        });
    }

    public CombatEntity getEntity(UUID id) {
        return entities.get(id);
    }

    public void removeEntity(UUID id) {
        CombatEntity entity = entities.remove(id);
        if (entity != null) {
            entity.handleSafeZoneEntry();
        }
    }
}
