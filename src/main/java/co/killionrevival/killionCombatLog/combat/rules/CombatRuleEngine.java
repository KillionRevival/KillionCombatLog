package co.killionrevival.killioncombatlog.combat.rules;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import lombok.extern.java.Log;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine that manages and evaluates combat rules.
 * Provides centralized combat detection logic.
 */
@Log
public class CombatRuleEngine {

    private final KillionCombatLog plugin;
    private final Map<Class<? extends Event>, List<ICombatRule<? extends Event>>> rulesByEvent;
    private boolean debug;

    public CombatRuleEngine(KillionCombatLog plugin) {
        this.plugin = plugin;
        this.rulesByEvent = new HashMap<>();
        this.debug = plugin.getConfig().getBoolean("settings.debug-mode", false);

        // Register default rules
        registerRule(new GameModeRule());
        registerRule(new ProjectileCombatRule());
    }

    /**
     * Registers a new combat rule.
     *
     * @param rule The rule to register
     */
    public <T extends Event> void registerRule(ICombatRule<T> rule) {
        rulesByEvent.computeIfAbsent(rule.getEventType(), k -> new ArrayList<>())
                .add(rule);

        if (debug) {
            log.info("Registered combat rule: " + rule.getClass().getSimpleName());
        }
    }

    /**
     * Evaluates an event against all registered rules for its type.
     *
     * @param event The event to evaluate
     * @return The combat result
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> CombatResult evaluateEvent(T event) {
        List<ICombatRule<? extends Event>> rules = rulesByEvent.get(event.getClass());

        if (rules == null || rules.isEmpty()) {
            return CombatResult.noCombat("No rules registered for event type: " +
                    event.getClass().getSimpleName());
        }

        for (ICombatRule<?> rule : rules) {
            ICombatRule<T> typedRule = (ICombatRule<T>) rule;
            CombatResult result = typedRule.evaluateInteraction(event);

            if (debug) {
                log.info(String.format("Rule %s evaluation: combat=%s, reason='%s'",
                        rule.getClass().getSimpleName(),
                        result.isShouldEnterCombat(),
                        result.getReason()));
            }

            if (result.isShouldEnterCombat()) {
                return result;
            }
        }

        return CombatResult.noCombat("No rules triggered combat");
    }

    /**
     * Sets the debug mode for the rule engine.
     *
     * @param debug Whether to enable debug logging
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
        if (debug) {
            log.info("Combat rule engine debug mode enabled");
        }
    }
}