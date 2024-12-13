package co.killionrevival.killioncombatlog.combat;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class CombatEntity {
    private final KillionCombatLog plugin;
    @Getter private final UUID playerId;
    @Setter @Getter private Player player;
    @Getter private Doppel doppel;
    private final Set<CombatSession> activeSessions = new HashSet<>();

    public CombatEntity(KillionCombatLog plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerId = player.getUniqueId();
    }

    public boolean hasDoppel() {
        return doppel != null;
    }

    public void handlePlayerDamage(Player attacker) {
        if (attacker.getUniqueId().equals(playerId)) return;

        CombatEntity attackerEntity = plugin.getEntityManager().getEntity(attacker);
        handleCombatSession(attackerEntity);
    }

    public void handleDoppelDamage(Player attacker) {
        if (attacker.getUniqueId().equals(playerId)) return;

        LogUtil.debug("Processing Doppel damage from " + attacker.getName());
        CombatEntity attackerEntity = plugin.getEntityManager().getEntity(attacker);
        CombatSession session = handleCombatSession(attackerEntity);

        if (session != null) {
            LogUtil.debug("Extending session time due to Doppel damage");
            session.handleDoppelSwap(playerId);
            LogUtil.debug("New session time: " + session.getRemainingSeconds() + "s");
        }
    }

    private CombatSession handleCombatSession(CombatEntity attackerEntity) {
        if (attackerEntity == null || attackerEntity == this) return null;

        // Check for existing session with either the player or their doppel
        CombatSession existingSession = getSessionWith(attackerEntity.getPlayerId());

        // Additional check for if attacker has a doppel
        if (existingSession == null && attackerEntity.hasDoppel()) {
            existingSession = getSessionWith(attackerEntity.getDoppel().getOwnerId());
        }

        // Additional check for if victim has a doppel
        if (existingSession == null && this.hasDoppel()) {
            existingSession = attackerEntity.getSessionWith(this.getDoppel().getOwnerId());
        }

        if (existingSession == null) {
            LogUtil.debug(String.format("Creating new combat session between %s and %s",
                    attackerEntity.getPlayerId(), this.getPlayerId()));
            CombatSession newSession = plugin.getCombatSessionManager().createSession(attackerEntity, this);

            // Link session to both entities
            attackerEntity.addSession(newSession);
            this.addSession(newSession);

            // Fire combat state events
            if (!attackerEntity.isInCombat() && attackerEntity.getPlayer() != null) {
                plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(attackerEntity.getPlayer(), true));
            }
            if (!isInCombat() && getPlayer() != null) {
                plugin.getServer().getPluginManager().callEvent(
                    new PlayerCombatStateChangedEvent(getPlayer(), true));
            }

            return newSession;
        } else {
            LogUtil.debug("Reengaging existing session");
            existingSession.handleReengagement(attackerEntity.getPlayerId());
            return existingSession;
        }
    }

    public void handleLogout(boolean isPvPZone) {
        LogUtil.debug(String.format("Handling logout for player %s. PvP Zone: %b, In Combat: %b",
                player.getName(), isPvPZone, isInCombat()));

        if (!isPvPZone) {
            LogUtil.debug("Non-PvP zone logout, ending combat sessions");
            for (CombatSession session : new HashSet<>(activeSessions)) {
                session.forceEnd(CombatEndReason.TIMER_EXPIRED);
                plugin.getCombatManager().endCombatSession(session, CombatEndReason.TIMER_EXPIRED);
            }
            activeSessions.clear();
            return;
        }

        if (player != null) {
            // Ensure we have the most current player state
            player.updateInventory();

            // Create fresh copies of inventory contents
            ItemStack[] currentInventory = player.getInventory().getContents().clone();
            ItemStack[] currentArmor = player.getInventory().getArmorContents().clone();

            // Log inventory contents for debugging
            LogUtil.debug("Current inventory contents at logout:");
            for (int i = 0; i < currentInventory.length; i++) {
                if (currentInventory[i] != null) {
                    LogUtil.debug(String.format("Slot %d: %s", i, currentInventory[i].getType()));
                }
            }

            Location exactLoc = player.getLocation();
            double exactX = exactLoc.getX();
            double exactY = exactLoc.getY();
            double exactZ = exactLoc.getZ();
            float exactYaw = exactLoc.getYaw();
            float exactPitch = exactLoc.getPitch();

            // Create a precise location object
            Location preciseLocation = new Location(
                    exactLoc.getWorld(),
                    exactX,
                    exactY,
                    exactZ,
                    exactYaw,
                    exactPitch
            );

            LogUtil.debug(String.format("Creating Doppel at precise location: x=%.6f, y=%.6f, z=%.6f, yaw=%.6f, pitch=%.6f",
                exactX, exactY, exactZ, exactYaw, exactPitch));

            // Store precise location
            plugin.getDatabaseManager().saveDoppelState(
                    player.getUniqueId(),
                    player.getHealth(),
                    currentInventory,    // Use fresh copy
                    currentArmor,        // Use fresh copy
                    String.format("%.6f,%6f,%6f,%6f,%6f,%s",
                            exactX, exactY, exactZ, exactYaw, exactPitch,
                            exactLoc.getWorld().getName())
            );

            // Create new Doppel with fresh inventory data
            this.doppel = new Doppel(
                    player.getUniqueId(),
                    player.getHealth(),
                    currentInventory,    // Use fresh copy
                    preciseLocation
            );

            plugin.getNPCManager().createNPC(doppel, player);

            if (!isInCombat()) {
                int defaultDuration = plugin.getConfigManager().getDoppelDefaultDuration();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (hasDoppel() && !isInCombat()) {
                            LogUtil.debug("Removing non-combat Doppel after timeout");
                            removeDoppel();
                        }
                    }
                }.runTaskLater(plugin, defaultDuration * 20L);
            }
        }
    }

    public void handleLoginWithDoppel(Player player) {
        this.player = player;
        try {
            if (doppel != null && doppel.getNpc() != null) {
                LogUtil.debug("Transferring Doppel state back to player " + player.getName());

                // Get max health
                double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();

                // Try to get health from different sources in order of preference
                double finalHealth;

                // Try NPC's current health first
                if (doppel.getNpc().getEntity() instanceof Player npcPlayer) {
                    finalHealth = npcPlayer.getHealth();
                    LogUtil.debug("Using NPC's current health: " + finalHealth);
                } else if (doppel.getNpc().data().has("final-health")) {
                    finalHealth = doppel.getNpc().data().get("final-health");
                    LogUtil.debug("Using stored final health: " + finalHealth);
                } else {
                    finalHealth = doppel.getHealth();
                    LogUtil.debug("Using original Doppel health: " + finalHealth);
                }

                // Validate and set health
                finalHealth = Math.max(0.1, Math.min(maxHealth, finalHealth));
                LogUtil.debug(String.format("Setting player health to %f (Max: %f)", finalHealth, maxHealth));
                player.setHealth(finalHealth);

                removeDoppel();

                if (isInCombat()) {
                    plugin.getServer().getPluginManager().callEvent(
                            new PlayerCombatStateChangedEvent(player, true)
                    );

                    int highestRemainingTime = activeSessions.stream()
                            .mapToInt(CombatSession::getRemainingSeconds)
                            .max().orElse(0);
                    String message = plugin.getConfigManager().getStillInCombatMessage()
                            .replace("%seconds%", String.valueOf(highestRemainingTime));
                    player.sendMessage(co.killionrevival.killioncombatlog.util.MessageUtility.chatComponent(message));
                }
            }
        } catch (Exception e) {
            LogUtil.error("Error while handling login with Doppel", e);
            // Set minimum health if something goes wrong
            player.setHealth(Math.max(0.1, Math.min(Objects.requireNonNull(
                player.getAttribute(Attribute.MAX_HEALTH)).getValue(), 1.0)));
        }
    }

    public void removeDoppel() {
        if (doppel != null) {
            LogUtil.debug("Removing Doppel for player " + playerId);
            plugin.getNPCManager().removeNPC(doppel.getNpc());
            doppel = null;
        }
    }

    public void handleSafeZoneEntry() {
        LogUtil.debug("Safe zone entry for " + playerId);
        for (CombatSession session : new HashSet<>(activeSessions)) {
            session.forceEnd(CombatEndReason.ENTERED_SAFE_ZONE);
            plugin.getCombatManager().endCombatSession(session, CombatEndReason.ENTERED_SAFE_ZONE);
        }
        activeSessions.clear();
        removeDoppel();
    }

    public boolean isInCombat() {
        return !activeSessions.isEmpty();
    }

    public CombatSession getSessionWith(UUID otherId) {
        return activeSessions.stream()
                .filter(session -> session.hasEntity(otherId))
                .findFirst()
                .orElse(null);
    }

    public void addSession(CombatSession session) {
        activeSessions.add(session);
        LogUtil.debug("Added combat session for " + playerId + ". Total sessions: " + activeSessions.size());
    }

    public void removeSession(CombatSession session) {
        activeSessions.remove(session);
        LogUtil.debug("Removed combat session for " + playerId + ". Remaining sessions: " + activeSessions.size());
    }

    public Set<CombatSession> getActiveSessionsSnapshot() {
        return new HashSet<>(activeSessions);
    }
}
