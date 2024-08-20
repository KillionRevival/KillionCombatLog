package co.killionrevival.killionCombatLog.npc;

import co.killionrevival.killioncommons.npc.IKillionNpc;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class LoggedOutPlayer implements IKillionNpc {
    private PlayerProfile playerProfile;
    boolean attackable;

    public LoggedOutPlayer(UUID npcUuid, String name) {
        playerProfile = Bukkit.createProfile(npcUuid, name);
        attackable = true;
    }

    @Override
    public PlayerProfile getPlayerRepresentation() {
        return playerProfile;
    }

    @Override
    public boolean getAttackable() {
        return attackable;
    }

    @Override
    public void setAttackable(boolean attackable) {
        this.attackable = attackable;
    }
}
