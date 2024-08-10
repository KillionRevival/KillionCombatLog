package co.killionrevival.killionCombatLog.managers;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killioncommons.KillionCommonsApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LogoutManager {
    private KillionCommonsApi api;
    private ConcurrentHashMap<UUID, Integer> logoutTasks;

    public LogoutManager() {
        logoutTasks = new ConcurrentHashMap<>();
        api = KillionCombatLog.getCommons();
    }

    public void startLogout(final Player player) {
        final NamespacedKey key = getLogoutBarNamespacedKey(player);
        final BossBar bar = Bukkit.createBossBar(key, "Logout", BarColor.YELLOW, BarStyle.SEGMENTED_10);
        bar.addPlayer(player);
        Bukkit.getScheduler().runTaskTimer(
                KillionCombatLog.getInstance(),
                (task) -> {
                    final BossBar bukkitBar = Bukkit.getBossBar(key);
                    if (bukkitBar == null) {
                        logoutTasks.remove(player.getUniqueId());
                        task.cancel();
                        KillionCombatLog.getCommons().getConsoleUtil()
                                .sendError("BossBar" + key.asString() + " was null when updating.");
                        return;
                    }
                    final double progress = bukkitBar.getProgress();
                    bukkitBar.getPlayers().forEach(
                            barPlayer -> api.getConsoleUtil().sendInfo("Player logout: " + barPlayer.getName() + " progress: " + progress)
                    );

                    if (progress >= 1) {
                        final TextComponent component = Component.text("Logout complete!");
                        logoutTasks.remove(player.getUniqueId());
                        task.cancel();
                        player.kick(component);
                        return;
                    }
                    if (!logoutTasks.containsKey(player.getUniqueId())) {
                        logoutTasks.put(player.getUniqueId(), task.getTaskId());
                    }
                    bukkitBar.setProgress(Math.min(progress + .1, 1));
                }, 0, 20
        );
    }

    public boolean isLoggingOut(final UUID uuid) {
        return logoutTasks.containsKey(uuid);
    }

    public boolean isLoggingOut(final Player player) {
        return isLoggingOut(player.getUniqueId());
    }

    public void cancelLogout(final Player player) {
        cancelLogout(player.getUniqueId());
    }

    public void cancelLogout(final UUID playerId) {
        Bukkit.getScheduler().cancelTask(logoutTasks.get(playerId));
        logoutTasks.remove(playerId);
        final NamespacedKey barKey = getLogoutBarNamespacedKey(playerId);
        final BossBar bar = Bukkit.getBossBar(barKey);
        if (bar != null) {
            bar.removeAll();
            Bukkit.removeBossBar(barKey);
        }
    }

    public NamespacedKey getLogoutBarNamespacedKey(final Player player) {
        return getLogoutBarNamespacedKey(player.getUniqueId());
    }

    public NamespacedKey getLogoutBarNamespacedKey(final UUID playerId) {
        return new NamespacedKey(KillionCombatLog.getInstance(), playerId + "logout");
    }

    public void cleanUp() {
        logoutTasks.keySet().stream()
                   .map(this::getLogoutBarNamespacedKey)
                   .forEach(Bukkit::removeBossBar);
        logoutTasks = new ConcurrentHashMap<>();
        Bukkit.getScheduler().cancelTasks(KillionCombatLog.getInstance());
    }
}
