package co.killionrevival.killionCombatLog.npc;

import co.killionrevival.killioncommons.npc.KillionNpc;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class LoggedOutPlayer extends KillionNpc {
    public LoggedOutPlayer(UUID npcUuid, String name, OfflinePlayer playerSkin) {
        this(npcUuid, name);
        setPlayerSkin(playerSkin);
    }
    public LoggedOutPlayer(UUID npcUuid, String name) {
        super();
        setNpcUuid(npcUuid);
        setName(name);
    }
}
