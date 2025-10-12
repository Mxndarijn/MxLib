package nl.mxndarijn.mxlib.permission;

import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MxPermissionService {

    private static MxPermissionService instance;

    private String basePrefix = "default-prefix";

    private MxPermissionService() {}

    public static void init(String prefix) {
        if (instance == null) {
            instance = new MxPermissionService();
            instance.basePrefix = prefix;
            Logger.logMessage(LogLevel.DEBUG, StandardPrefix.PERMISSION_SERVICE,
                    "PermissionService initialized with prefix: " + prefix);
        } else {
            Logger.logMessage(LogLevel.WARNING, StandardPrefix.PERMISSION_SERVICE,
                    "PermissionService is already initialized!");
        }
    }

    public static MxPermissionService getInstance() {
        if (instance == null)
            throw new IllegalStateException("PermissionService not intialized!");
        return instance;
    }

    private String buildFull(String node) {
        if (node == null || node.isEmpty()) return basePrefix;
        if (node.startsWith(basePrefix + ".")) return node;
        return basePrefix + "." + node;
    }

    public boolean hasPlayerPermission(Player player, PermissionType permission) {
        return player.hasPermission(buildFull(permission.node()));
    }

    public boolean hasSenderPermission(CommandSender sender, PermissionType permission) {
        return sender.hasPermission(buildFull(permission.node()));
    }
}
