package nl.mxndarijn.mxlib.permission;

import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.permission.PermissionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PermissionService {

    private static PermissionService instance;

    private final Map<String, String> permissions = new ConcurrentHashMap<>();
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

    public static PermissionService get() {
        if (instance == null)
            throw new IllegalStateException("PermissionService not intialized!");
        return instance;
    }

    public <E extends Enum<E> & PermissionType> void registerAll(Class<E> enumClass) {
        for (E perm : enumClass.getEnumConstants()) {
            register(perm);
        }
    }
    public void register(PermissionType permission) {
        String fullNode = buildFull(permission.node());
        permissions.put(permission.node(), fullNode);
    }

    private String buildFull(String node) {
        if (node == null || node.isEmpty()) return basePrefix;
        if (node.startsWith(basePrefix + ".")) return node; // dubbel prefix vermijden
        return basePrefix + "." + node;
    }

    public String getFull(PermissionType permission) {
        return permissions.getOrDefault(permission.node(), buildFull(permission.node()));
    }

    public Collection<String> all() {
        return Collections.unmodifiableCollection(permissions.values());
    }
}
