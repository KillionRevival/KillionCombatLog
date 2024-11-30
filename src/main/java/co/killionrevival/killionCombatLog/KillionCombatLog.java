package co.killionrevival.killioncombatlog;

import co.killionrevival.killioncombatlog.combat.CombatManager;
import co.killionrevival.killioncombatlog.combat.listeners.CombatDeathListener;
import co.killionrevival.killioncombatlog.combat.listeners.CombatDisconnectListener;
import co.killionrevival.killioncombatlog.combat.listeners.CombatStateListener;
import co.killionrevival.killioncombatlog.core.commands.KCLCommand;
import co.killionrevival.killioncombatlog.logger.CombatLogManager;
import co.killionrevival.killioncombatlog.npc.NPCManager;
import co.killionrevival.killioncombatlog.npc.TraitManager;
import co.killionrevival.killioncombatlog.util.LogUtil;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * The main class of the KillionCombatLog plugin.
 * <p>
 * This plugin addresses the issue of combat logging by spawning an NPC when a player disconnects during combat.
 * The NPC represents the player and can be attacked by others, ensuring that combat consequences are enforced.
 * </p>
 */
public final class KillionCombatLog extends JavaPlugin {

    @Getter private CombatManager combatManager;
    @Getter private CombatLogManager combatLogManager;
    @Getter private TraitManager traitManager;
    private NPCManager npcManager;

    public NPCManager getNPCManager() {
        return this.npcManager;
    }

    @Override
    public void onLoad() {
        try {
            getLogger().info("KillionCombatLog is loading...");
            // Add any onLoad initialization if needed
        } catch (Exception e) {
            getLogger().severe("Error during plugin load: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean checkDependencies() {
        getLogger().info("Checking dependencies...");

        String[] requiredPlugins = {"WorldGuard", "Citizens", "KillionCommons"};
        boolean allDependenciesPresent = true;

        for (String pluginName : requiredPlugins) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin == null || !plugin.isEnabled()) {
                getLogger().severe("Required dependency " + pluginName + " is missing or not enabled!");
                allDependenciesPresent = false;
            } else {
                getLogger().info("Found dependency: " + pluginName + " v" + plugin.getDescription().getVersion());
            }
        }

        return allDependenciesPresent;
    }

    /**
     * Initializes the plugin's configuration and managers.
     * Loads default settings and prepares the plugin for operation.
     */
    private void initialize() {
        LogUtil.info("Initializing plugin configuration...");
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);

        LogUtil.debug("Loading default configuration values...");
        this.getConfig().addDefault("messages.in-combat", "&cYou are now in combat &4&l>> &a%seconds% seconds.");
        this.getConfig().addDefault("messages.no-longer", "&cYou are no longer in combat.");
        this.getConfig().addDefault("settings.npc-lifetime-seconds", 30);
        this.getConfig().addDefault("settings.combat-tag-duration", 30);
        this.saveConfig();
        LogUtil.info("Configuration initialized successfully");

        LogUtil.info("Initializing managers...");
        this.combatManager = new CombatManager(this);
        this.combatLogManager = new CombatLogManager(this);
        this.traitManager = new TraitManager();
        this.npcManager = new NPCManager(this);
        LogUtil.info("All managers initialized successfully");
    }

    /**
     * Registers event listeners and commands.
     * Ensures that the plugin responds to game events and player commands appropriately.
     */
    private void registerComponents() {
        LogUtil.info("Registering event listeners...");
        this.getServer().getPluginManager().registerEvents(new CombatStateListener(this), this);
        this.getServer().getPluginManager().registerEvents(new CombatDeathListener(this), this);
        this.getServer().getPluginManager().registerEvents(new CombatDisconnectListener(this), this);
        this.getServer().getPluginManager().registerEvents(
            new co.killionrevival.killioncombatlog.logger.listeners.CombatLogListener(this), this
        );
        LogUtil.info("Event listeners registered successfully");

        LogUtil.info("Registering commands...");
        KCLCommand kclCommand = new KCLCommand(this);
        Objects.requireNonNull(this.getCommand("killioncombatlog")).setExecutor(kclCommand);
        Objects.requireNonNull(this.getCommand("killioncombatlog")).setTabCompleter(kclCommand);
        LogUtil.info("Commands registered successfully");
    }

    /**
     * Performs startup routines such as logging plugin activation.
     */
    private void startPlugin() {
        LogUtil.info("Plugin loaded successfully.");
    }

    /**
     * Performs shutdown routines such as closing database connections.
     */
    private void stopPlugin() {
        LogUtil.info("Shutting down plugin...");
        if (this.combatManager != null) {
            this.combatManager.close();
        }
        LogUtil.info("Plugin stopped successfully");
    }

    @Override
    public void onEnable() {
        LogUtil.init(this);
        initialize();
        registerComponents();
        startPlugin();
    }

    @Override
    public void onDisable() {
        stopPlugin();
    }
}