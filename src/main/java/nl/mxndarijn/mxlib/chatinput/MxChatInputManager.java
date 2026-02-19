package nl.mxndarijn.mxlib.chatinput;

import io.papermc.paper.event.player.AsyncChatEvent;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.util.Functions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class MxChatInputManager implements Listener {

    private static MxChatInputManager instance;
    private final HashMap<UUID, MxChatInputCallback> map;

    private MxChatInputManager(JavaPlugin plugin) {
        map = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.MXCHATINPUT_MANAGER, "MxChatInputManager loaded...");

    }

    public static void init(JavaPlugin plugin) {
        instance = new MxChatInputManager(plugin);
    }

    public static MxChatInputManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MxChatInputManager is not initialized!");
        }
        return instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chatEvent(AsyncChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (map.containsKey(uuid)) {
            e.setCancelled(true);
            MxChatInputCallback inputCallback = map.get(uuid);
            map.remove(uuid);
            inputCallback.textReceived(Functions.convertComponentToString(e.message()));
        }
    }

    public String addChatInputCallback(UUID uuid, MxChatInputCallback callback) {
        map.put(uuid, callback);
        return null;
    }
}
