package co.killionrevival.killioncombatlog.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;

/**
 * Utility class for WorldGuard related checks
 */
public class WorldGuardHelper {

    /**
     * Checks if PvP is enabled at a given location
     * @param location The location to check
     * @return true if PvP is enabled at this location
     */
    public static boolean isPvPEnabled(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));

            if (regions == null) {
                // If no region manager, fallback to world's PvP setting
                return location.getWorld().getPVP();
            }

            BlockVector3 position = BlockVector3.at(
                    location.getX(),
                    location.getY(),
                    location.getZ()
            );

            // Get the PvP flag value at this location
            StateFlag.State pvpState = regions.getApplicableRegions(position)
                    .queryValue(null, Flags.PVP);

            // If no PvP flag is set, or it's set to ALLOW
            if (pvpState == null || pvpState == StateFlag.State.ALLOW) {
                return location.getWorld().getPVP();
            }

            // If PvP is explicitly denied
            return pvpState != StateFlag.State.DENY;

        } catch (Exception e) {
            LogUtil.error("Error checking WorldGuard PvP status", e);
            // Fallback to world's PvP setting if there's an error
            return location.getWorld().getPVP();
        }
    }
}