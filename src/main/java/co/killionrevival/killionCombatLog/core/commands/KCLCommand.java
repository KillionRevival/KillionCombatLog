package co.killionrevival.killioncombatlog.core.commands;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command executor and tab completer for the /killioncombatlog command.
 * Allows authorized players to interact with the plugin's administrative functions.
 */
public class KCLCommand implements CommandExecutor, TabCompleter {

    private final KillionCombatLog plugin;

    /**
     * Constructor to initialize the command with the main plugin instance.
     *
     * @param plugin The main plugin instance.
     */
    public KCLCommand(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the execution of the /killioncombatlog command.
     *
     * @param sender  The sender of the command.
     * @param command The command object.
     * @param label   The command label.
     * @param args    The command arguments.
     * @return True if the command was processed successfully; otherwise, false.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Ensure the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtility.colorize("&cThis is a player-only command."));
            return true;
        }

        Player player = (Player) sender;

        // Check for permission
        if (!player.hasPermission("killioncombatlog.reload")) {
            player.sendMessage(MessageUtility.colorize("&cYou lack permission to execute this command."));
            return true;
        }

        // Ensure at least one argument is provided
        if (args.length < 1) {
            return false;
        }

        String action = args[0];

        // Handle command actions
        if (action.equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            player.sendMessage(MessageUtility.chatComponent("&aConfiguration reloaded successfully!"));
        } else {
            player.sendMessage(MessageUtility.chatComponent("&cUnknown command. Use /" + label + " reload."));
        }

        return true;
    }

    /**
     * Provides tab completion suggestions for the /killioncombatlog command.
     *
     * @param sender  The sender of the command.
     * @param command The command object.
     * @param label   The command label.
     * @param args    The command arguments.
     * @return A list of possible completions for the last argument.
     */
    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        java.util.List<String> completions = new java.util.ArrayList<>();

        // Suggest "reload" if the first argument is being typed
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
