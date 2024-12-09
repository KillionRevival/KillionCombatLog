package co.killionrevival.killioncombatlog.commands;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KCLCommand implements CommandExecutor, TabCompleter {

    private final KillionCombatLog plugin;

    public KCLCommand(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtility.colorize("&cThis is a player-only command."));
            return true;
        }

        if (!player.hasPermission("killioncombatlog.reload")) {
            player.sendMessage(MessageUtility.colorize("&cYou lack permission to execute this command."));
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        String action = args[0];

        if (action.equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reloadConfig();
            player.sendMessage(MessageUtility.chatComponent("&aConfiguration reloaded successfully!"));
        } else {
            player.sendMessage(MessageUtility.chatComponent("&cUnknown command. Use /" + label + " reload."));
        }

        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        java.util.List<String> completions = new java.util.ArrayList<>();

        if (args.length == 1) {
            String[] commands = {"reload"};
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        }

        return completions;
    }
}
