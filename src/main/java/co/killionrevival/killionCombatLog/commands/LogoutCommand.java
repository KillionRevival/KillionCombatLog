package co.killionrevival.killionCombatLog.commands;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killionCombatLog.managers.LogoutManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LogoutCommand implements TabExecutor {
    final LogoutManager manager;

    public LogoutCommand() {
        this.manager = KillionCombatLog.getInstance().getLogoutManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof final Player player)) {
            return true;
        }
        manager.startLogout(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.singletonList("logout");
    }
}
