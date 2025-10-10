package nl.mxndarijn.mxlib;

import lombok.Getter;
import nl.mxndarijn.mxlib.changeworld.ChangeWorldManager;
import nl.mxndarijn.mxlib.changeworld.MxChangeWorld;
import nl.mxndarijn.mxlib.chatinput.MxChatInputManager;
import nl.mxndarijn.mxlib.chatprefix.ChatPrefixManager;
import nl.mxndarijn.mxlib.configfiles.ConfigService;
import nl.mxndarijn.mxlib.configfiles.StandardConfigFile;
import nl.mxndarijn.mxlib.inventory.MxInventoryManager;
import nl.mxndarijn.mxlib.language.LanguageManager;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.PrefixRegistry;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.permission.PermissionService;
import org.bukkit.plugin.java.JavaPlugin;

public class MxLib {
    @Getter
    private static JavaPlugin plugin;


    public static void init(JavaPlugin p, String permissionPrefix) {
        plugin = p;
        Logger.logMessage(LogLevel.INFORMATION, "Loading MxLib...");
        ConfigService.init(plugin);
        LanguageManager.getInstance();
        MxChatInputManager.init(plugin);
        ChatPrefixManager.getInstance();
        PrefixRegistry.registerAll(StandardPrefix.class);
        ConfigService.getInstance().registerAll(StandardConfigFile.class);
        PermissionService.init(permissionPrefix);
        MxInventoryManager.init(plugin);
        ChangeWorldManager.init(plugin);
        Logger.logMessage(LogLevel.INFORMATION, "MxLib Loaded.");
    }
}
