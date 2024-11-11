package co.killionrevival.killionCombatLog.managers;

import co.killionrevival.killionCombatLog.KillionCombatLog;
import co.killionrevival.killionCombatLog.traits.CombatLogTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class TraitManager {

    private final KillionCombatLog plugin;
    public TraitManager(KillionCombatLog plugin) {
        this.plugin = plugin;

        CitizensAPI.getTraitFactory().registerTrait(
                TraitInfo.create(CombatLogTrait.class).withName("CombatLogTrait")
        );
    }
}
