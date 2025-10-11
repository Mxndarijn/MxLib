package nl.mxndarijn.mxlib.permission;

import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PermissionService {

    private static PermissionService instance;

    private String basePrefix = "default-prefix";

    private PermissionService() {}

    public static void init(String prefix) {
        if (instance == null) {
            instance = new PermissionService();
            instance.basePrefix = prefix;
            Logger.logMessage(LogLevel.DEBUG, StandardPrefix.PERMISSION_SERVICE,
                    "PermissionService initialized with prefix: " + prefix);
        } else {
            Logger.logMessage(LogLevel.WARNING, StandardPrefix.PERMISSION_SERVICE,
                    "PermissionService is already initialized!");
        }
    }

    public static PermissionService getInstance() {
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
}
