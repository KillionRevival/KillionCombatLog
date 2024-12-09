package co.killionrevival.killioncombatlog;

import co.killionrevival.killioncombatlog.combat.*;
import co.killionrevival.killioncombatlog.combat.listeners.*;
import co.killionrevival.killioncombatlog.commands.KCLCommand;
import co.killionrevival.killioncombatlog.config.ConfigManager;
import co.killionrevival.killioncombatlog.logger.CombatLogManager;
import co.killionrevival.killioncombatlog.npc.NPCManager;
import co.killionrevival.killioncombatlog.npc.TraitManager;
import co.killionrevival.killioncombatlog.util.LogUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for KillionCombatLog.
 * Initializes and manages all systems including Combat Entities, Sessions, Doppels, and Logging.
 */
public final class KillionCombatLog extends JavaPlugin {
    @Getter
    private CombatSessionManager combatSessionManager;
    @Getter
    private CombatEntityManager entityManager;
    @Getter
    private CombatManager combatManager;
    @Getter
    private CombatLogManager combatLogManager;
    private NPCManager npcManager;
    private TraitManager traitManager;
    @Getter
    private ConfigManager configManager;

    @Override
    public void onLoad() {
        getLogger().info("KillionCombatLog is loading...");
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

        // Initialize ConfigManager
        this.configManager = new ConfigManager(this);
        configManager.loadConfig();
        LogUtil.setDebug(configManager.isDebugMode());

        // Load combat settings from ConfigManager
        int initialDuration = configManager.getCombatTagDuration();
        int reengagementDuration = configManager.getReengagementDuration();
        int doppelSwapDuration = configManager.getDoppelSwapDuration();
        int maxDuration = configManager.getMaxDuration();
        long maxSessionLength = configManager.getMaxSessionLength();

        // Initialize managers
        this.entityManager = new CombatEntityManager(this);
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

        LogUtil.info("KillionCombatLog enabled successfully.");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
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

        LogUtil.info("KillionCombatLog disabled successfully.");
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }

    public TraitManager getTraitManager() {
        return traitManager;
    }

}
