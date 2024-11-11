package co.killionrevival.killionCombatLog.commands;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killionCombatLog.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.colorize("&cThis is a player-only command."));
            return true;
        }

        Player player = (Player) sender;

        // Check for permission
        if (!player.hasPermission("killioncombatlog.reload")) {
            player.sendMessage(Utils.colorize("&cYou lack permission to execute this command."));
            return true;
        }

        // Ensure at least one argument is provided
        if (args.length < 1) {
            return false;
        }

        String action = args[0];

        // Handle command actions
        switch (action.toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                player.sendMessage(Utils.chatComponent("&aConfiguration reloaded successfully!"));
                break;
            default:
                player.sendMessage(Utils.chatComponent("&cUnknown command. Use /" + label + " reload."));
                break;
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
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
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
