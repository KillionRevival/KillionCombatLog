package co.killionrevival.killioncombatlog.util;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtil {
    private static final String PREFIX = "[KillionCombatLog] ";
    private static Logger logger;

    @Setter
    private static boolean debug = false;

    public static void init(KillionCombatLog plugin) {
        logger = plugin.getLogger();
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warning(message);
    }

    public static void error(String message, Throwable e) {
        logger.log(Level.SEVERE, message, e);
    }

    public static void debug(String message) {
        if (debug) {
            logger.info("[DEBUG] " + message);
        }
    }

    public static void combat(String message) {
        logger.info(PREFIX + message);
    }
}
