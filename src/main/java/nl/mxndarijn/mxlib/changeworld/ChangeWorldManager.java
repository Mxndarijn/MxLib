package nl.mxndarijn.mxlib.changeworld;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChangeWorldManager implements Listener {

    private static ChangeWorldManager instance;

    private final HashMap<UUID, List<MxChangeWorld>> worlds;

    private ChangeWorldManager(JavaPlugin plugin) {
        Logger.logMessage(LogLevel.DEBUG, StandardPrefix.CHANGEWORLD_MANAGER, "Loading...");
        worlds = new HashMap<>();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    public static void init(JavaPlugin plugin) {
        instance = new ChangeWorldManager(plugin);
    }

    public static ChangeWorldManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ChangeWorldManager is not initialized!");
        }
        return instance;
    }

    @EventHandler
    public void changeWorld(PlayerChangedWorldEvent e) {
        UUID from = e.getFrom().getUID();
        UUID to = e.getPlayer().getWorld().getUID();
        if (from != to) {
            if (worlds.containsKey(from)) {
                worlds.get(from).forEach(mxChangeWorld -> mxChangeWorld.leave(e.getPlayer(), e.getFrom(), e));
            } else {
                Logger.logMessage(LogLevel.DEBUG, StandardPrefix.CHANGEWORLD_MANAGER, "World: " + e.getFrom().getName() + " not found (leaving this world). (" + e.getPlayer().getName() + ")");
            }
        }
        if (worlds.containsKey(to)) {
            worlds.get(to).forEach(mxChangeWorld -> mxChangeWorld.enter(e.getPlayer(), e.getFrom(), e));
        } else {
            Logger.logMessage(LogLevel.DEBUG, StandardPrefix.CHANGEWORLD_MANAGER, "World: " + e.getPlayer().getWorld().getName() + " not found (going to this world). (" + e.getPlayer().getName() + ")");
        }
    }

    @EventHandler
    public void worldUnload(WorldUnloadEvent e) {
        UUID worldUID = e.getWorld().getUID();
        if (worlds.containsKey(worldUID)) {
            Logger.logMessage(LogLevel.DEBUG, StandardPrefix.CHANGEWORLD_MANAGER, "World: " + e.getWorld().getName() + " has been unloaded.");
            worlds.remove(worldUID);

        }
    }

    public void addWorld(UUID uid, MxChangeWorld changeWorld) {
        List<MxChangeWorld> list = worlds.containsKey(uid) ? worlds.get(uid) : new ArrayList<>();
        list.add(changeWorld);
        worlds.put(uid, list);
    }

    public void removeWorld(UUID uid) {
        worlds.remove(uid);
    }
}
