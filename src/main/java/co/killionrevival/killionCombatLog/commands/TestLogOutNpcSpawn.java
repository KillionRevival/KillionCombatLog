package co.killionrevival.killionCombatLog.commands;

import co.killionrevival.killionCombatLog.npc.LoggedOutPlayer;
import co.killionrevival.killioncommons.KillionCommons;
import co.killionrevival.killioncommons.npc.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestLogOutNpcSpawn implements TabExecutor {
    final NpcManager npcManager;

    public TestLogOutNpcSpawn() {
        npcManager = KillionCommons.getInstance().getNpcManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player player)) {
            return true;
        }

        if (args.length == 0) {
            // should spawn an npc at the player's location with their UUID and name.
            npcManager.spawn(
                    new LoggedOutPlayer(UUID.randomUUID(), player.getName(), Bukkit.getOfflinePlayer(player.getUniqueId())),
                    player.getLocation()
            );
        }
        else {
            // should spawn an npc at the player's location with their UUID and name.
            npcManager.spawn(
                    new LoggedOutPlayer(UUID.randomUUID(), player.getName(), Bukkit.getOfflinePlayer(args[0])),
                    player.getLocation()
            );
        }


        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.singletonList("testlogoutnpc");
    }
}
