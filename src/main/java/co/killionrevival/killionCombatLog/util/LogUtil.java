package co.killionrevival.killioncombatlog.util;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for standardized logging throughout the plugin.
 */
public class LogUtil {
    private static final String PREFIX = "[KillionCombatLog] ";
    private static Logger logger;
    /**
     * -- SETTER --
     *  Sets the debug mode status.
     */
    @Setter
    private static boolean debug = false;

    /**
     * Initializes the logger with the plugin instance.
     *
     * @param plugin The main plugin instance
     */
    public static void init(KillionCombatLog plugin) {
        logger = plugin.getLogger();
        debug = plugin.getConfig().getBoolean("settings.debug-mode", false);
    }

    /**
     * Logs an info message.
     *
     * @param message The message to log
     */
    public static void info(String message) {
        logger.info(PREFIX + message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    public static void warn(String message) {
        logger.warning(PREFIX + message);
    }

    /**
     * Logs an error message with an exception.
     *
     * @param message The message to log
     * @param e The exception to log
     */
    public static void error(String message, Throwable e) {
        logger.log(Level.SEVERE, PREFIX + message, e);
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param message The message to log
     */
    public static void debug(String message) {
        if (debug) {
            logger.info(PREFIX + "[DEBUG] " + message);
        }
    }

    /**
     * Logs a combat-related message.
     *
     * @param message The message to log
     */
    public static void combat(String message) {
        logger.info(PREFIX + "[KillionCombatLog] " + message);
    }
}