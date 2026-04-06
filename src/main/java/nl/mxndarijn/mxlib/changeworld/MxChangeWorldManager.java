package nl.mxndarijn.mxlib.changeworld;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Manager for world change events.
 * Registers and notifies {@link MxChangeWorld} listeners when players move between worlds.
 */
public class MxChangeWorldManager implements Listener {

    private static MxChangeWorldManager instance;

    private final HashMap<UUID, List<MxChangeWorld>> worlds;
    private final List<MxChangeWorld> unspecificWorlds = new ArrayList<>();

    private MxChangeWorldManager(JavaPlugin plugin) {
        MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.CHANGEWORLD_MANAGER, "Loading...");
        worlds = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    /**
     * Initializes the singleton instance.
     * @param plugin the {@link JavaPlugin} instance
     */
    public static void init(JavaPlugin plugin) {
        instance = new MxChangeWorldManager(plugin);
    }

    /**
     * Gets the singleton instance.
     * @return the {@code MxChangeWorldManager} instance
     * @throws IllegalStateException if not initialized
     */
    public static MxChangeWorldManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ChangeWorldManager is not initialized!");
        }
        return instance;
    }

    /**
     * Event handler for {@link PlayerChangedWorldEvent}.
     * @param e the event
     */
    @EventHandler
    public void changeWorld(PlayerChangedWorldEvent e) {
        UUID from = e.getFrom().getUID();
        UUID to = e.getPlayer().getWorld().getUID();
        World toWorld = Bukkit.getWorld(to);
        if (!from.equals(to)) {
            MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.CHANGEWORLD_MANAGER, "Player " + e.getPlayer().getName() + " changed world from " + e.getFrom().getName() + " to " + e.getPlayer().getWorld().getName());
            unspecificWorlds.forEach(mxChangeWorld -> {
                mxChangeWorld.leave(e.getPlayer(), e.getFrom(), e);
                mxChangeWorld.enter(e.getPlayer(), toWorld, e);
            });
            if (worlds.containsKey(from)) {
                worlds.get(from).forEach(mxChangeWorld -> mxChangeWorld.leave(e.getPlayer(), e.getFrom(), e));
            } else {
                MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.CHANGEWORLD_MANAGER, "World: " + e.getFrom().getName() + " not found (leaving this world). (" + e.getPlayer().getName() + ")");
            }

            if (worlds.containsKey(to)) {
                worlds.get(to).forEach(mxChangeWorld -> mxChangeWorld.enter(e.getPlayer(), toWorld, e));
            } else {
                MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.CHANGEWORLD_MANAGER, "World: " + e.getPlayer().getWorld().getName() + " not found (going to this world). (" + e.getPlayer().getName() + ")");
            }
        }
    }

    /**
     * Event handler for {@link WorldUnloadEvent}.
     * @param e the event
     */
    @EventHandler
    public void worldUnload(WorldUnloadEvent e) {
        UUID worldUID = e.getWorld().getUID();
        if (worlds.containsKey(worldUID)) {
            MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.CHANGEWORLD_MANAGER, "World: " + e.getWorld().getName() + " has been unloaded.");
            worlds.remove(worldUID);

        }
    }

    /**
     * Event handler for {@link PlayerQuitEvent}.
     * @param e the event
     */
    @EventHandler
    public void quit(PlayerQuitEvent e) {
        World w = e.getPlayer().getWorld();
        UUID worldUID = w.getUID();
        unspecificWorlds.forEach(mxChangeWorld -> mxChangeWorld.quit(e.getPlayer(), w, e));
        if (worlds.containsKey(worldUID)) {
            worlds.get(worldUID).forEach(mxChangeWorld -> mxChangeWorld.quit(e.getPlayer(), w, e));
        }
    }

    /**
     * Adds a listener for a specific world.
     * @param uid the world UUID
     * @param changeWorld the listener
     */
    public void addWorld(UUID uid, MxChangeWorld changeWorld) {
        List<MxChangeWorld> list = worlds.getOrDefault(uid, new ArrayList<>());
        list.add(changeWorld);
        worlds.put(uid, list);
    }

    /**
     * Adds a listener that triggers for all world changes.
     * @param changeWorld the listener
     */
    public void addUnspecificWorld(MxChangeWorld changeWorld) {
        unspecificWorlds.add(changeWorld);
    }

    /**
     * Removes all listeners for a specific world.
     * @param uid the world UUID
     */
    public void removeWorld(UUID uid) {
        worlds.remove(uid);
    }
}
