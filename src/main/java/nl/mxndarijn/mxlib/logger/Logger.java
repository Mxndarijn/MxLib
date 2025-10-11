package nl.mxndarijn.mxlib.logger;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

public class Logger {
    @Setter
    private static LogLevel logLevel = LogLevel.DEBUG;
    @Getter
    private static String prefix = "<dark_gray>[<gold>MxLib";

    public static void setMainPrefix(String p) {
        prefix = p;
    }

    public static void logMessage(LogLevel level, String message) {
        if (level.getLevel() <= logLevel.getLevel()) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("<!i>" + level.getPrefix() + message));
        }
    }

    public static void logMessage(String message) {
        logMessage(LogLevel.DEBUG_HIGHLIGHT, message);
    }

    public static void logMessage(LogLevel level, PrefixType prefix, String message) {
        if (level.getLevel() <= logLevel.getLevel()) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("<!i>" + level.getPrefix() + prefix + message));
        }
    }
}
