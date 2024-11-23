package co.killionrevival.killioncombatlog.npc;

import co.killionrevival.killioncombatlog.npc.traits.CombatLogTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

/**
 * Manages the registration of custom traits for NPCs.
 * Registers the CombatLogTrait with the Citizens API.
 */
public class TraitManager {

    /**
     * Constructor to register custom traits.
     */
    public TraitManager() {
        CitizensAPI.getTraitFactory().registerTrait(
                TraitInfo.create(CombatLogTrait.class).withName("CombatLogTrait")
        );
    }
}
