package co.killionrevival.killioncombatlog.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;

public class WorldGuardHelper {
    public static boolean isPvPEnabled(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        try {
            com.sk89q.worldguard.protection.regions.RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));

            if (regions == null) {
                return location.getWorld().getPVP();
            }

            BlockVector3 position = BlockVector3.at(location.getX(), location.getY(), location.getZ());
            StateFlag.State pvpState = regions.getApplicableRegions(position).queryValue(null, Flags.PVP);

            if (pvpState == null || pvpState == StateFlag.State.ALLOW) {
                return location.getWorld().getPVP();
            }

            return pvpState != StateFlag.State.DENY;

        } catch (Exception e) {
            LogUtil.error("Error checking WorldGuard PvP status", e);
            return location.getWorld().getPVP();
        }
    }
}
