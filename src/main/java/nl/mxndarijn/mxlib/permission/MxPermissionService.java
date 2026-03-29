package nl.mxndarijn.mxlib.permission;

import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
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
            MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.PERMISSION_SERVICE,
                    "PermissionService initialized with prefix: " + prefix);
        } else {
            MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.PERMISSION_SERVICE,
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

    public boolean hasPlayerPermission(Player player, MxPermissionType permission) {
        return player.hasPermission(buildFull(permission.node()));
    }

    public boolean hasSenderPermission(CommandSender sender, MxPermissionType permission) {
        return sender.hasPermission(buildFull(permission.node()));
    }
}
