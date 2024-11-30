package co.killionrevival.killioncombatlog.combat.rules;

import org.bukkit.event.Event;

/**
 * Interface defining combat rules that determine if players should enter combat.
 * Each rule can process different types of combat interactions.
 */
public interface ICombatRule<T extends Event> {

    /**
     * Determines if the interaction should result in combat.
     *
     * @param event The combat interaction event
     * @return CombatResult containing the decision and involved players
     */
    CombatResult evaluateInteraction(T event);

    /**
     * Gets the event class this rule handles.
     *
     * @return The Class object of the event type
     */
    Class<T> getEventType();
}