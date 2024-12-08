package co.killionrevival.killioncombatlog;

import co.killionrevival.killioncombatlog.combat.*;
import co.killionrevival.killioncombatlog.combat.listeners.*;
import co.killionrevival.killioncombatlog.commands.KCLCommand;
import co.killionrevival.killioncombatlog.logger.CombatLogManager;
import co.killionrevival.killioncombatlog.logger.listeners.CombatLogListener;
import co.killionrevival.killioncombatlog.npc.NPCManager;
import co.killionrevival.killioncombatlog.npc.TraitManager;
import co.killionrevival.killioncombatlog.util.LogUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class KillionCombatLog extends JavaPlugin {
    private CombatSessionManager combatSessionManager;
    private CombatEntityManager entityManager;
    private CombatManager combatManager;
    private CombatLogManager combatLogManager;
    private NPCManager npcManager;  // Make sure this has @Getter
    private TraitManager traitManager;

    public NPCManager getNPCManager() {  // Explicit getter for NPCManager
        return npcManager;
    }

    @Override
    public void onLoad() {
        try {
            getLogger().info("KillionCombatLog is loading...");
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

    @Override
    public void onEnable() {
        LogUtil.init(this);

        if (!checkDependencies()) {
            getLogger().severe("Missing required dependencies - disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load configuration
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        setupDefaultConfig();

        // Load combat settings
        int initialDuration = getConfig().getInt("settings.combat-tag-duration", 30);
        int reengagementDuration = getConfig().getInt("settings.reengagement-duration", 10);
        int doppelSwapDuration = getConfig().getInt("settings.doppel-swap-duration", 15);
        int maxDuration = getConfig().getInt("settings.max-duration", 60);
        long maxSessionLength = getConfig().getLong("settings.max-session-length", 300);

        // Initialize managers in correct order
        this.entityManager = new CombatEntityManager();

        this.combatSessionManager = new CombatSessionManager(
                this,
                initialDuration,
                reengagementDuration,
                doppelSwapDuration,
                maxDuration,
                maxSessionLength
        );

        this.npcManager = new NPCManager(this);
        this.traitManager = new TraitManager();
        this.combatManager = new CombatManager(this);
        this.combatLogManager = new CombatLogManager(this);

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        LogUtil.info("Plugin enabled successfully.");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CombatLogListener(this), this);
        pm.registerEvents(new CombatDisconnectListener(this), this);
        pm.registerEvents(new CombatDeathListener(this), this);
        pm.registerEvents(new CombatAttackListener(this), this);
    }

    private void registerCommands() {
        KCLCommand kclCommand = new KCLCommand(this);
        getCommand("killioncombatlog").setExecutor(kclCommand);
        getCommand("killioncombatlog").setTabCompleter(kclCommand);
    }

    @Override
    public void onDisable() {
        // Cleanup in reverse order of initialization
        if (combatManager != null) {
            combatManager.shutdown();
        }
        if (combatSessionManager != null) {
            combatSessionManager.shutdown();
        }
        if (entityManager != null) {
            entityManager.shutdown();
        }
        if (combatLogManager != null) {
            // Any cleanup needed for CombatLogManager
        }
        if (npcManager != null) {
            // Any cleanup needed for NPCManager
        }

        LogUtil.info("Plugin disabled successfully.");
    }

    /**
     * Adds default values to config if they don't exist
     */
    private void setupDefaultConfig() {
        getConfig().addDefault("settings.combat-tag-duration", 30);
        getConfig().addDefault("settings.reengagement-duration", 10);
        getConfig().addDefault("settings.doppel-swap-duration", 15);
        getConfig().addDefault("settings.max-duration", 60);
        getConfig().addDefault("settings.max-session-length", 300);
        getConfig().addDefault("settings.debug-mode", false);

        getConfig().addDefault("messages.in-combat", "&cYou are in combat for %seconds% more seconds!");
        getConfig().addDefault("messages.no-longer", "&aYou are no longer in combat.");
        getConfig().addDefault("messages.combat-engaged", "&cYou have entered combat!");
        getConfig().addDefault("messages.combat-engaged-projectile", "&cYou have entered combat due to projectile!");
        getConfig().addDefault("messages.combat-death", "&c%victim% was slain by %killer% in combat!");
        getConfig().addDefault("messages.combat-kill", "&aYou are no longer in combat after defeating %victim%!");
        getConfig().addDefault("messages.combat-log-death", "&cYou were killed by &6%killer%&c while logged out.");
        getConfig().addDefault("messages.still-in-combat", "&cYou are still in combat!");

        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}