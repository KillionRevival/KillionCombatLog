package co.killionrevival.killionCombatLog.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility class providing helper methods for text formatting and time conversion.
 */
public class Utils {

    /**
     * Converts a string with legacy color codes (e.g., '&a') into a Component.
     * Suitable for sending messages to players.
     *
     * @param text The input text containing legacy color codes.
     * @return A Component representing the formatted text.
     */
    public static Component chatComponent(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    /**
     * Translates color codes in the input string to their corresponding color codes using '§'.
     * Suitable for methods requiring a String with color codes.
     *
     * @param text The input text containing color codes (e.g., '&aHello World')
     * @return A string with color codes translated using '§' symbol.
     */
    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('&', '§');
    }

    /**
     * Converts a time duration in milliseconds to a human-readable format.
     * Formats the duration into hours, minutes, and seconds.
     *
     * @param milliseconds The time duration in milliseconds.
     * @return A formatted string representing the time duration.
     */
    public static String formatTime(long milliseconds) {
        final long ONE_SECOND = 1000;
        final long ONE_MINUTE = 60 * ONE_SECOND;
        final long ONE_HOUR = 60 * ONE_MINUTE;

        long hours = milliseconds / ONE_HOUR;
        milliseconds %= ONE_HOUR;

        long minutes = milliseconds / ONE_MINUTE;
        milliseconds %= ONE_MINUTE;

        long seconds = milliseconds / ONE_SECOND;

        StringBuilder timeString = new StringBuilder();

        if (hours > 0) {
            timeString.append(hours).append(" hours");
        }

        if (minutes > 0) {
            if (timeString.length() > 0) {
                timeString.append(", ");
            }
            timeString.append(minutes).append(" minutes");
        }

        if (seconds > 0) {
            if (timeString.length() > 0) {
                timeString.append(", ");
            }
            timeString.append(seconds).append(" seconds");
        }

        if (timeString.length() <= 0) {
            return "0 seconds";
        }

        return timeString.toString();
    }
}
