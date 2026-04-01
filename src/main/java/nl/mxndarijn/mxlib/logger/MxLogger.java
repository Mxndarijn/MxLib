package nl.mxndarijn.mxlib.logger;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

public class MxLogger {
    @Setter
    private static MxLogLevel logLevel = MxLogLevel.DEBUG;
    @Getter
    private static String prefix = "<dark_gray>[<gold>MxLib";

    public static void setMainPrefix(String p) {
        prefix = p;
    }

    public static void logMessage(MxLogLevel level, String message) {
        if (level.getLevel() <= logLevel.getLevel()) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("<!i>" + level.getPrefix() + message));
        }
    }

    public static void logMessage(String message) {
        logMessage(MxLogLevel.DEBUG_HIGHLIGHT, message);
    }

    public static void logMessage(MxLogLevel level, MxPrefixType prefix, String message) {
        if (level.getLevel() <= logLevel.getLevel()) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize("<!i>" + level.getPrefix() + prefix + message));
        }
    }

    public static void logMessage(MxLogLevel level, Component component) {
        if (level.getLevel() <= logLevel.getLevel()) {
            Bukkit.getConsoleSender().sendMessage(component);
        }
    }

    public static void logMessage(MxLogLevel level, MxPrefixType prefix, Component component) {
        if (level.getLevel() <= logLevel.getLevel()) {
            Component full = MiniMessage.miniMessage().deserialize("<!i>" + level.getPrefix() + prefix).append(component);
            Bukkit.getConsoleSender().sendMessage(full);
        }
    }
}
