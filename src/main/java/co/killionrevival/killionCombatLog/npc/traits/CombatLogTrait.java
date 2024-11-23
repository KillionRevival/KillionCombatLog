package co.killionrevival.killioncombatlog.npc.traits;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.MessageUtility;
import lombok.Setter;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * Custom trait for NPCs representing combat loggers.
 * Manages the NPC's behavior, including damage handling, death, and hologram updates.
 */
public class CombatLogTrait extends Trait implements Listener {

    private final KillionCombatLog plugin = KillionCombatLog.getPlugin(KillionCombatLog.class);
    private int secondsLeft;
    private double currentAbsorption = 0.0;
    /**
     * -- SETTER --
     *  Sets the parent player of the NPC.
     *
     */
    @Setter
    private Player parentPlayer;
    private BukkitRunnable countdownTask;
    private HologramTrait hologramTrait;

    /**
     * Constructor to initialize the trait.
     */
    public CombatLogTrait() {
        super("CombatLogTrait");
        this.secondsLeft = plugin.getConfig().getInt("settings.npc-lifetime-seconds");
    }

    @Override
    public void onAttach() {
        startCountdown();
        setupHologram();
    }

    @Override
    public void onRemove() {
        cancelCountdown();
        clearHologram();
    }

    /**
     * Event handler for when the NPC takes damage.
     * Adjusts health and absorption accordingly.
     *
     * @param event The NPCDamageByEntityEvent.
     */
    @EventHandler
    public void onNPCDamage(NPCDamageByEntityEvent event) {
        if (event.getNPC() != this.getNPC() || !(event.getDamager() instanceof Player)) {
            return;
        }

        event.setCancelled(false);

        double damage = event.getDamage();
        double finalDamage = calculateFinalDamage((Player) getNPC().getEntity(), damage);

        handleAbsorption(event, finalDamage);
        resetCountdown((int) damage);
    }

    /**
     * Event handler for when the NPC dies.
     * Drops the NPC's inventory and marks the player as dead.
     *
     * @param event The NPCDeathEvent.
     */
    @EventHandler
    public void onNPCDeath(NPCDeathEvent event) {
        if (event.getNPC() != this.getNPC()) {
            return;
        }

        Player npcPlayer = (Player) getNPC().getEntity();
        dropPlayerInventory(npcPlayer);

        getNPC().despawn();
        getNPC().destroy();

        String killerName = npcPlayer.getKiller() != null ? npcPlayer.getKiller().getName() : "unknown";
        plugin.getCombatManager().setPlayerAsDead(parentPlayer.getUniqueId(), killerName, true);
    }

    /**
     * Sets the current absorption amount for the NPC.
     *
     * @param absorption The absorption amount.
     */
    public void setAbsorption(double absorption) {
        this.currentAbsorption = absorption / 2;
    }

    /**
     * Handles absorption effects when the NPC takes damage.
     *
     * @param event       The NPCDamageByEntityEvent.
     * @param finalDamage The calculated final damage.
     */
    private void handleAbsorption(NPCDamageByEntityEvent event, double finalDamage) {
        if (currentAbsorption > 0) {
            if (currentAbsorption > finalDamage) {
                currentAbsorption -= finalDamage;
                event.setDamage(0);
            } else {
                event.setDamage(finalDamage - currentAbsorption);
                currentAbsorption = 0;
            }
        } else {
            currentAbsorption = 0;
        }
    }

    /**
     * Handles NPC death by dropping its inventory.
     */
    private void dropPlayerInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }

    /**
     * Starts the countdown timer for the NPC's lifetime.
     */
    private void startCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    despawnNPC();
                    cancel();
                    return;
                }

                secondsLeft--;
                updateHologram();
            }
        };

        countdownTask.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Cancels the countdown timer.
     */
    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    /**
     * Resets the countdown timer when the NPC takes damage.
     *
     * @param damage The amount of damage taken.
     */
    private void resetCountdown(int damage) {
        secondsLeft += damage;
        updateHologram();
    }

    /**
     * Sets up the hologram display above the NPC.
     */
    private void setupHologram() {
        hologramTrait = getNPC().getOrAddTrait(HologramTrait.class);
        updateHologram();
    }

    /**
     * Clears the hologram when the trait is removed.
     */
    private void clearHologram() {
        if (hologramTrait != null) {
            hologramTrait.clear();
        }
    }

    /**
     * Updates the hologram to reflect the current countdown and health.
     */
    private void updateHologram() {
        if (hologramTrait == null) {
            return;
        }

        Player npcPlayer = (Player) getNPC().getEntity();
        if (npcPlayer == null) {
            plugin.getLogger().warning("Entity is not a Player!");
            return;
        }

        hologramTrait.setLine(0, MessageUtility.colorize("&a" + MessageUtility.formatTime(secondsLeft * 1000L) + "."));
        hologramTrait.setLine(1, MessageUtility.colorize("&c&lDISCONNECT: &7" + getNPC().getName()));
        hologramTrait.setLine(2, MessageUtility.colorize(getPrettyHearts(npcPlayer.getHealth() / 2, 10)));
    }

    /**
     * Generates a visual representation of the NPC's health and absorption using heart icons.
     *
     * @param health    The current health in half-hearts.
     * @param maxHealth The maximum health in half-hearts.
     * @return A string representing the health status.
     */
    private String getPrettyHearts(double health, double maxHealth) {
        StringBuilder heartsString = new StringBuilder();

        for (int heart = 0; heart < maxHealth; heart++) {
            if (heart < health) {
                heartsString.append("&c❤");
            } else {
                heartsString.append("&7❤");
            }
        }

        for (int i = 0; i < currentAbsorption; i++) {
            heartsString.append("&e❤");
        }

        return heartsString.toString();
    }

    /**
     * Calculates the final damage after accounting for armor and toughness.
     *
     * @param player The player entity.
     * @param damage The initial damage.
     * @return The final damage after reductions.
     */
    private double calculateFinalDamage(Player player, double damage) {
        double armor = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ARMOR)).getValue();
        double toughness = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)).getValue();

        return damage * (1 - (Math.min(20.0, Math.max(armor / 5.0, armor - damage / (2.0 + toughness / 4.0))) / 25.0));
    }

    /**
     * Despawns and destroys the NPC when the countdown reaches zero.
     */
    private void despawnNPC() {
        getNPC().despawn();
        getNPC().destroy();
    }
}
