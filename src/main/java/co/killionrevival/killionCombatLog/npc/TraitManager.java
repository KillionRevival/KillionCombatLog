package co.killionrevival.killioncombatlog.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

/**
 * Registers custom NPC traits.
 */
public class TraitManager {
    public TraitManager() {
        CitizensAPI.getTraitFactory().registerTrait(
                TraitInfo.create(CombatLogTrait.class).withName("CombatLogTrait")
        );
    }
}
