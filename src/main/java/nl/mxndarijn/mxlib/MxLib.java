package nl.mxndarijn.mxlib;

import lombok.Getter;
import nl.mxndarijn.mxlib.changeworld.ChangeWorldManager;
import nl.mxndarijn.mxlib.chatprefix.ChatPrefixManager;
import nl.mxndarijn.mxlib.configfiles.ConfigService;
import nl.mxndarijn.mxlib.configfiles.StandardConfigFile;
import nl.mxndarijn.mxlib.inventory.MxInventoryManager;
import nl.mxndarijn.mxlib.logger.PrefixRegistry;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.permission.PermissionService;
import org.bukkit.plugin.java.JavaPlugin;

public class MxLib {
    @Getter
    private static JavaPlugin plugin;


    public static void init(JavaPlugin p, String permissionPrefix) {
        plugin = p;
        ConfigService.init(plugin);
        ChatPrefixManager.getInstance();
        PrefixRegistry.registerAll(StandardPrefix.class);
        ConfigService.getInstance().registerAll(StandardConfigFile.class);
        PermissionService.init(permissionPrefix);
        MxInventoryManager.init(plugin);
        ChangeWorldManager.init(plugin);
    }
}
