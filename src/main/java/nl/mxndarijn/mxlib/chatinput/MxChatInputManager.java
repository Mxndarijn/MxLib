package nl.mxndarijn.mxlib.chatinput;

import io.papermc.paper.event.player.AsyncChatEvent;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.util.MxFunctions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manager for capturing chat input from players.
 * When a player is registered, their next chat message is intercepted and passed to a callback.
 */
public class MxChatInputManager implements Listener {

    private static MxChatInputManager instance;
    private final HashMap<UUID, MxChatInputCallback> map;
    private final JavaPlugin plugin;

    private MxChatInputManager(JavaPlugin plugin) {
        this.plugin = plugin;
        map = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        MxLogger.logMessage(MxLogLevel.INFORMATION, MxStandardPrefix.MXCHATINPUT_MANAGER, "MxChatInputManager loaded...");

    }

    /**
     * Initializes the singleton instance.
     * @param plugin the {@link JavaPlugin} instance
     */
    public static void init(JavaPlugin plugin) {
        instance = new MxChatInputManager(plugin);
    }

    /**
     * Gets the singleton instance.
     * @return the {@code MxChatInputManager} instance
     * @throws IllegalStateException if not initialized
     */
    public static MxChatInputManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MxChatInputManager is not initialized!");
        }
        return instance;
    }

    /**
     * Handles chat events to intercept input from registered players.
     * @param e the chat event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void chatEvent(AsyncChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (map.containsKey(uuid)) {
            e.setCancelled(true);
            MxChatInputCallback inputCallback = map.get(uuid);
            map.remove(uuid);
            String message = MxFunctions.convertComponentToString(e.message());
            Bukkit.getScheduler().runTask(plugin, () -> inputCallback.textReceived(message));
        }
    }

    /**
     * Registers a callback to capture the next chat message from a player.
     * @param uuid the player's UUID
     * @param callback the callback to trigger
     * @return always {@code null}
     */
    public String addChatInputCallback(UUID uuid, MxChatInputCallback callback) {
        map.put(uuid, callback);
        return null;
    }
}
