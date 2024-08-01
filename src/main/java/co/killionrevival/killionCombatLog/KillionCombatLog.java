package co.killionrevival.killionCombatLog;

import co.killionrevival.killioncommons.KillionCommonsApi;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class KillionCombatLog extends JavaPlugin {
    public static StateFlag KILLION_COMBAT_LOG_WORLDGUARD_FLAG;

    @Getter
    static KillionCombatLog instance;

    @Getter
    static KillionCommonsApi commons;

    @Override
    public void onEnable() {
        instance = this;
        commons = new KillionCommonsApi(this);
        commons.getConsoleUtil().sendInfo("KillionCombatLog initialized");

        if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            registerWorldGuardFlags();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerWorldGuardFlags() {
        commons.getConsoleUtil().sendInfo("Registering WorldGuard flags");
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("kcombatlog-safe", true);
            registry.register(flag);
            KILLION_COMBAT_LOG_WORLDGUARD_FLAG = flag;
        } catch (FlagConflictException e) {
            commons.getConsoleUtil().sendError("Error initializing flag");
            Flag<?> existing = registry.get("kcombatlog-safe");
            if (existing instanceof StateFlag) {
                commons.getConsoleUtil().sendError("Existing flag was typed correctly - setting instance to existing flag.");
                KILLION_COMBAT_LOG_WORLDGUARD_FLAG = (StateFlag) existing;
            } else {
                commons.getConsoleUtil().sendError("Flag instance was not typed correctly?! How?!");
                commons.getConsoleUtil().sendThrowable(e);
            }
        }
        commons.getConsoleUtil().sendInfo("Done!");
    }
}
