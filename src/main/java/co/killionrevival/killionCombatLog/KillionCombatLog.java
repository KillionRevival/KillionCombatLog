package co.killionrevival.killionCombatLog;

import co.killionrevival.killionCombatLog.commands.KCLCommand;
import co.killionrevival.killionCombatLog.listeners.CombatLogListener;
import co.killionrevival.killionCombatLog.listeners.PlayerListener;
import co.killionrevival.killionCombatLog.managers.CombatLogManager;
import co.killionrevival.killionCombatLog.managers.CombatManager;
import co.killionrevival.killionCombatLog.managers.NPCManager;
import co.killionrevival.killionCombatLog.managers.TraitManager;
import co.killionrevival.killionCombatLog.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public final class KillionCombatLog extends JavaPlugin {

    private CombatManager combatManager;
    private CombatLogManager combatLogManager;
    private TraitManager traitManager;
    private NPCManager npcManager;

    public CombatManager getCombatManager() {
        return this.combatManager;
    }
    public CombatLogManager getCombatLogManager() { return this.combatLogManager; }
    public TraitManager getTraitManager() {
        return this.traitManager;
    }
    public NPCManager getNPCManager() {
        return this.npcManager;
    }

    void init() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.getConfig().addDefault("messages.in-combat", "&cYou are now in combat &4&l>> &a%seconds% seconds.");
        this.getConfig().addDefault("messages.no-longer", "&cYou are no longer in combat.");
        this.getConfig().addDefault("settings.npc-lifetime-seconds", 30);
        this.getConfig().addDefault("settings.combat-tag-duration", 30);
        this.saveConfig();

        this.combatManager = new CombatManager(this);
        this.combatLogManager = new CombatLogManager(this);
        this.traitManager = new TraitManager(this);
        this.npcManager = new NPCManager(this);
    }

    void register() {
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new CombatLogListener(this), this);

        KCLCommand flclCommand = new KCLCommand(this);
        this.getCommand("killionCombatLog").setExecutor(flclCommand);
        this.getCommand("killionCombatLog").setTabCompleter(flclCommand);
    }
    void start() {
        this.getServer().getConsoleSender().sendMessage(Utils.chat("[KillionCombatLog] &aLoaded KillionCombatLog."));
    }
    void stop() {
        this.combatManager.close();
        this.getServer().getConsoleSender().sendMessage(Utils.chat("[KillionCombatLog] &cStopped KillionCombatLog."));
    }

    @Override
    public void onEnable() {
        this.init();
        this.register();
        this.start();
    }

    @Override
    public void onDisable() {
        this.stop();
    }
}
