package co.killionrevival.killionCombatLog;

import co.killionrevival.killionCombatLog.commands.KCLCommand;
import co.killionrevival.killionCombatLog.listeners.CombatLogListener;
import co.killionrevival.killionCombatLog.listeners.PlayerListener;
import co.killionrevival.killionCombatLog.managers.*;
import co.killionrevival.killionCombatLog.utils.Utils;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the KillionCombatLog plugin.
 * <p>
 * This plugin addresses the issue of combat logging by spawning an NPC when a player disconnects during combat.
 * The NPC represents the player and can be attacked by others, ensuring that combat consequences are enforced.
 * </p>
 */
public final class KillionCombatLog extends JavaPlugin {

    // Getters for manager instances
    @Getter
    private CombatManager combatManager;
    @Getter
    private CombatLogManager combatLogManager;
    @Getter
    private TraitManager traitManager;
    private NPCManager npcManager;

    public NPCManager getNPCManager() {
        return this.npcManager;
    }

    /**
     * Initializes the plugin's configuration and managers.
     * Loads default settings and prepares the plugin for operation.
     */
    private void initialize() {
        // Save and load default configuration
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);

        // Add default messages and settings
        this.getConfig().addDefault("messages.in-combat", "&cYou are now in combat &4&l>> &a%seconds% seconds.");
        this.getConfig().addDefault("messages.no-longer", "&cYou are no longer in combat.");
        this.getConfig().addDefault("settings.npc-lifetime-seconds", 30);
        this.getConfig().addDefault("settings.combat-tag-duration", 30);

        // Save the configuration
        this.saveConfig();

        // Initialize manager instances
        this.combatManager = new CombatManager(this);
        this.combatLogManager = new CombatLogManager(this);
        this.traitManager = new TraitManager(); // Updated to match constructor
        this.npcManager = new NPCManager(this);
    }

    /**
     * Registers event listeners and commands.
     * Ensures that the plugin responds to game events and player commands appropriately.
     */
    private void registerComponents() {
        // Register event listeners
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new CombatLogListener(this), this);

        // Register command executors and tab completers
        KCLCommand kclCommand = new KCLCommand(this);
        this.getCommand("killionCombatLog").setExecutor(kclCommand);
        this.getCommand("killionCombatLog").setTabCompleter(kclCommand);
    }

    /**
     * Performs startup routines such as logging plugin activation.
     */
    private void startPlugin() {
        this.getServer().getConsoleSender().sendMessage(Utils.colorize("[KillionCombatLog] &aPlugin loaded successfully."));
    }

    /**
     * Performs shutdown routines such as closing database connections.
     */
    private void stopPlugin() {
        this.combatManager.close();
        this.getServer().getConsoleSender().sendMessage(Utils.colorize("[KillionCombatLog] &cPlugin stopped."));
    }

    @Override
    public void onEnable() {
        initialize();
        registerComponents();
        startPlugin();
    }

    @Override
    public void onDisable() {
        stopPlugin();
    }
}
