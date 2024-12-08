package co.killionrevival.killioncombatlog.config;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final KillionCombatLog plugin;
    private FileConfiguration config;

    @Getter private final Map<String, String> messages = new HashMap<>();
    @Getter private boolean debugMode;
    @Getter private int combatTagDuration;
    @Getter private int reengagementDuration;
    @Getter private int doppelSwapDuration;
    @Getter private int maxDuration;
    @Getter private long maxSessionLength;
    @Getter private int minimumOnlinePlayers;
    @Getter private boolean doppelOnlyInCombat;
    @Getter private boolean customQuitMessages;
    @Getter private boolean customDeathMessages;

    // Combat Settings
    @Getter private boolean rejoinPenaltiesEnabled;
    @Getter private double rejoinHealthPenalty;

    // NPC Settings
    @Getter private boolean npcGlow;
    @Getter private int npcDespawnDelay;
    @Getter private boolean npcInvulnerable;

    public ConfigManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadSettings();
        loadMessages();
        validate();
    }

    private void loadSettings() {
        // Debug Settings
        debugMode = config.getBoolean("settings.debug-mode", false);
        LogUtil.setDebug(debugMode);

        // Combat Timers
        combatTagDuration = config.getInt("settings.combat-tag-duration", 30);
        reengagementDuration = config.getInt("settings.reengagement-duration", 10);
        doppelSwapDuration = config.getInt("settings.doppel-swap-duration", 15);
        maxDuration = config.getInt("settings.max-duration", 60);
        maxSessionLength = config.getLong("settings.max-session-length", 300);

        // General Settings
        minimumOnlinePlayers = config.getInt("settings.minimum-online-players", 1);
        doppelOnlyInCombat = config.getBoolean("settings.doppel-only-in-combat", true);
        customQuitMessages = config.getBoolean("settings.custom-quit-messages", true);
        customDeathMessages = config.getBoolean("settings.custom-death-messages", true);

        // Combat Settings
        rejoinPenaltiesEnabled = config.getBoolean("settings.rejoin-penalties.enabled", true);
        rejoinHealthPenalty = config.getDouble("settings.rejoin-penalties.health-percent", 20.0);

        // NPC Settings
        npcGlow = config.getBoolean("settings.npc.glow", true);
        npcDespawnDelay = config.getInt("settings.npc.despawn-delay", 300);
        npcInvulnerable = config.getBoolean("settings.npc.invulnerable", false);
    }

    private void loadMessages() {
        messages.clear();

        if (!config.isConfigurationSection("messages")) {
            LogUtil.warn("No messages section found in config!");
            return;
        }

        for (String key : config.getConfigurationSection("messages").getKeys(false)) {
            String message = config.getString("messages." + key);
            if (message != null) {
                messages.put(key, MessageUtility.colorize(message));
            }
        }
    }

    private void validate() {
        boolean valid = true;

        // Validate combat timers
        if (combatTagDuration <= 0) {
            LogUtil.error("Invalid combat-tag-duration: Must be greater than 0", null);
            valid = false;
        }
        if (maxDuration < combatTagDuration) {
            LogUtil.error("Invalid max-duration: Must be greater than combat-tag-duration", null);
            valid = false;
        }
        if (reengagementDuration <= 0) {
            LogUtil.error("Invalid reengagement-duration: Must be greater than 0", null);
            valid = false;
        }
        if (doppelSwapDuration <= 0) {
            LogUtil.error("Invalid doppel-swap-duration: Must be greater than 0", null);
            valid = false;
        }

        // Validate other numeric settings
        if (minimumOnlinePlayers < 0) {
            LogUtil.error("Invalid minimum-online-players: Must be 0 or greater", null);
            valid = false;
        }
        if (rejoinHealthPenalty < 0 || rejoinHealthPenalty > 100) {
            LogUtil.error("Invalid rejoin-health-penalty: Must be between 0 and 100", null);
            valid = false;
        }
        if (npcDespawnDelay <= 0) {
            LogUtil.error("Invalid npc-despawn-delay: Must be greater than 0", null);
            valid = false;
        }

        if (!valid) {
            LogUtil.error("Configuration validation failed - using default values", null);
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Missing message: " + key);
    }

    public String getMessage(String key, String... replacements) {
        String message = getMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }
}