package nl.mxndarijn.mxlib;

import lombok.Getter;
import nl.mxndarijn.mxlib.changeworld.MxChangeWorldManager;
import nl.mxndarijn.mxlib.chatinput.MxChatInputManager;
import nl.mxndarijn.mxlib.chatprefix.MxChatPrefixManager;
import nl.mxndarijn.mxlib.configfiles.MxConfigService;
import nl.mxndarijn.mxlib.configfiles.MxStandardConfigFile;
import nl.mxndarijn.mxlib.inventory.MxInventoryManager;
import nl.mxndarijn.mxlib.language.MxLanguageManager;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxPrefixRegistry;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.mxworld.MxAtlas;
import nl.mxndarijn.mxlib.permission.MxPermissionService;
import org.bukkit.plugin.java.JavaPlugin;

public class MxLib {
    @Getter
    private static JavaPlugin plugin;


    public static void init(JavaPlugin p, String permissionPrefix, String loggerPrefix) {
        plugin = p;
        MxLogger.setMainPrefix(loggerPrefix);
        MxLogger.logMessage(MxLogLevel.INFORMATION, "Loading MxLib...");
        MxConfigService.init(plugin);
        MxAtlas.init(plugin);
        MxLanguageManager.getInstance();
        MxChatInputManager.init(plugin);
        MxChatPrefixManager.getInstance();
        MxPrefixRegistry.registerAll(MxStandardPrefix.class);
        MxConfigService.getInstance().registerAll(MxStandardConfigFile.class);
        MxPermissionService.init(permissionPrefix);
        MxInventoryManager.init(plugin);
        MxChangeWorldManager.init(plugin);
        MxLogger.logMessage(MxLogLevel.INFORMATION, "MxLib Loaded.");
    }
}
