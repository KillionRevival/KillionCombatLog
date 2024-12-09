package co.killionrevival.killioncombatlog.config;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final KillionCombatLog plugin;
    private FileConfiguration config;

    // Configuration fields
    @Getter
    private int combatTagDuration;
    @Getter
    private int reengagementDuration;
    @Getter
    private int doppelSwapDuration;
    @Getter
    private int maxDuration;
    @Getter
    private long maxSessionLength;
    @Getter
    private boolean debugMode;
    @Getter
    private boolean customDeathMessages;

    // Messages
    @Getter
    private String inCombatMessage;
    @Getter
    private String noLongerMessage;
    @Getter
    private String combatEngagedMessage;
    @Getter
    private String combatEngagedProjectileMessage;
    @Getter
    private String combatDeathMessage;
    @Getter
    private String combatKillMessage;
    @Getter
    private String combatLogDeathMessage;
    @Getter
    private String stillInCombatMessage;
    @Getter
    private String offlineDeathMessage;

    public ConfigManager(KillionCombatLog plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the configuration from the config.yml file, sets defaults, and caches values.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig(); // Ensure latest values are loaded
        this.config = plugin.getConfig();
        setDefaults();
        cacheValues();
    }

    /**
     * Reloads the configuration and updates cached values.
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        cacheValues();
    }

    /**
     * Sets default values for config keys if they are not present.
     */
    private void setDefaults() {
        config.addDefault("settings.combat-tag-duration", 30);
        config.addDefault("settings.reengagement-duration", 10);
        config.addDefault("settings.doppel-swap-duration", 15);
        config.addDefault("settings.max-duration", 60);
        config.addDefault("settings.max-session-length", 300);
        config.addDefault("settings.debug-mode", false);
        config.addDefault("settings.custom-death-messages", true);

        config.addDefault("messages.in-combat", "&cYou are in combat for %seconds% more seconds!");
        config.addDefault("messages.no-longer", "&aYou are no longer in combat.");
        config.addDefault("messages.combat-engaged", "&cYou have entered combat!");
        config.addDefault("messages.combat-engaged-projectile", "&cYou have entered combat due to projectile!");
        config.addDefault("messages.combat-death", "&c%victim% was slain by %killer% in combat!");
        config.addDefault("messages.combat-kill", "&aYou are no longer in combat after defeating %victim%!");
        config.addDefault("messages.combat-log-death", "&cYou were killed by &6%killer%&c while logged out.");
        config.addDefault("messages.still-in-combat", "&cYou are still in combat!");
        config.addDefault("messages.offline-death", "&cYou were killed by &6%killer%&c while logged out.");

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * Reads values from the config into memory.
     */
    private void cacheValues() {
        // Settings
        combatTagDuration = config.getInt("settings.combat-tag-duration", 30);
        reengagementDuration = config.getInt("settings.reengagement-duration", 10);
        doppelSwapDuration = config.getInt("settings.doppel-swap-duration", 15);
        maxDuration = config.getInt("settings.max-duration", 60);
        maxSessionLength = config.getLong("settings.max-session-length", 300);
        debugMode = config.getBoolean("settings.debug-mode", false);
        customDeathMessages = config.getBoolean("settings.custom-death-messages", true);

        // Messages
        inCombatMessage = config.getString("messages.in-combat", "&cYou are in combat for %seconds% more seconds!");
        noLongerMessage = config.getString("messages.no-longer", "&aYou are no longer in combat.");
        combatEngagedMessage = config.getString("messages.combat-engaged", "&cYou have entered combat!");
        combatEngagedProjectileMessage = config.getString("messages.combat-engaged-projectile", "&cYou have entered combat due to projectile!");
        combatDeathMessage = config.getString("messages.combat-death", "&c%victim% was slain by %killer% in combat!");
        combatKillMessage = config.getString("messages.combat-kill", "&aYou are no longer in combat after defeating %victim%!");
        combatLogDeathMessage = config.getString("messages.combat-log-death", "&cYou were killed by &6%killer%&c while logged out.");
        stillInCombatMessage = config.getString("messages.still-in-combat", "&cYou are still in combat!");
        offlineDeathMessage = config.getString("messages.offline-death", "&cYou were killed by &6%killer%&c while logged out.");
    }

}
