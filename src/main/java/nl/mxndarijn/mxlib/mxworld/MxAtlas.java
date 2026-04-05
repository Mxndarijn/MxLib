package nl.mxndarijn.mxlib.mxworld;

import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.util.MxFunctions;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Manager for {@link MxWorld} instances.
 * Handles loading, unloading, duplicating, and deleting worlds.
 */
public class MxAtlas {
    private static JavaPlugin plugin;
    private static MxAtlas instance;
    private final ArrayList<MxWorld> worlds;

    /**
     * Sets the plugin instance for MxAtlas.
     * @param plugin the {@link JavaPlugin} instance
     */
    public static void setPlugin(JavaPlugin plugin) {
        MxAtlas.plugin = plugin;
    }

    private MxAtlas(JavaPlugin plugin) {
        MxLogger.logMessage(MxLogLevel.INFORMATION, MxStandardPrefix.MXATLAS, "Started MxAtlas... (World-Manager)");
        worlds = new ArrayList<>();
        setPlugin(plugin);
    }

    /**
     * Gets the singleton instance of MxAtlas.
     * @return the MxAtlas instance
     * @throws IllegalStateException if MxAtlas is not initialized
     */
    public static MxAtlas getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MxAtlas is not initialized!");
        }
        return instance;
    }

    /**
     * Initializes the MxAtlas singleton.
     * @param plugin the {@link JavaPlugin} instance
     * @throws IllegalStateException if MxAtlas is already initialized
     */
    public static void init(JavaPlugin plugin) {
        if(instance != null) {
            throw new IllegalStateException("MxAtlas is already initialized!");
        }
        else {
            instance = new MxAtlas(plugin);
        }
    }

    /**
     * Finds an {@link MxWorld} by its name.
     * @param name the name of the world
     * @return an {@link Optional} containing the world if found
     */
    public Optional<MxWorld> getMxWorld(String name) {
        for (MxWorld w : worlds) {
            if (w.getName().equals(name)) {
                return Optional.of(w);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds an {@link MxWorld} by its UUID.
     * @param uuid the UUID of the world
     * @return an {@link Optional} containing the world if found
     */
    public Optional<MxWorld> getMxWorld(UUID uuid) {
        for (MxWorld w : worlds) {
            if (w.getUUID().equalsIgnoreCase(uuid.toString())) {
                return Optional.of(w);
            }
        }
        return Optional.empty();
    }

    /**
     * Adds an {@link MxWorld} to the manager's tracking list.
     * @param world the world to add
     * @return true if added successfully
     */
    public boolean addMxWorld(MxWorld world) {
        return worlds.add(world);
    }

    /**
     * Removes an {@link MxWorld} from the manager's tracking list.
     * @param world the world to remove
     * @return true if removed successfully
     */
    public boolean removeMxWorld(MxWorld world) {
        return worlds.remove(world);
    }

    /**
     * Loads an {@link MxWorld} using the default configuration.
     * @param mxWorld the world to load
     * @return a {@link CompletableFuture} completing to true if success
     */
    public CompletableFuture<Boolean> loadMxWorld(MxWorld mxWorld) {
        return loadMxWorld(mxWorld, MxWorldLoadConfig.defaults());
    }

    /**
     * Loads an {@link MxWorld} with custom configuration.
     * @param mxWorld the world to load
     * @param config the load configuration
     * @return a {@link CompletableFuture} completing to true if success
     */
    public CompletableFuture<Boolean> loadMxWorld(MxWorld mxWorld, MxWorldLoadConfig config) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXATLAS, "Loading MxWorld: " + mxWorld.getName());
        if (mxWorld.isLoaded()) {
            MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.MXATLAS, mxWorld.getName() + " is already loaded.");
            future.complete(true);
            return future;
        }
        String path = mxWorld.getDir().toString().replace("\\", "/");
        WorldCreator wc = new WorldCreator(path);
        wc.environment(config.getEnvironment());
        wc.type(config.getWorldType());
        if (config.getChunkGenerator() != null) {
            wc.generator(config.getChunkGenerator());
        }
        wc.generateStructures(config.isGenerateStructures());
        File dir = mxWorld.getDir();
        try {
            MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXATLAS,
                    "About to load world from path: raw=" + dir +
                            ", abs=" + dir.getAbsolutePath() +
                            ", canonical=" + dir.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = wc.createWorld();
            if (world == null) {
                future.complete(false);
                return;
            }
            MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXATLAS, "Loading worldsettings.yml... ");
            MxWorldSettings worldSettings = MxWorldSettings.load(mxWorld.getDir());
            world.setAutoSave(worldSettings.isAutoSaveEnabled());
            int radius = worldSettings.getSpawnChunksForceLoadedRadius();

            if (radius > 0) {
                var spawnLoc = world.getSpawnLocation();
                int centerX = spawnLoc.getBlockX() >> 4;
                int centerZ = spawnLoc.getBlockZ() >> 4;

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        world.setChunkForceLoaded(centerX + dx, centerZ + dz, true);
                    }
                }
            }

            MxGameRuleUtil.applyGameRules(world, worldSettings.getGameRules());

            if (worldSettings.getSpawn() != null) {
                MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXATLAS, "Setting spawnlocation... ");
                world.setSpawnLocation(MxFunctions.getLocationFromConfiguration(world, worldSettings.getSpawn()));
            }

            mxWorld.setWorldUID(world.getUID());
            mxWorld.setLoaded(true);

            future.complete(true);
        });

        return future;
    }


    /**
     * Unloads an {@link MxWorld}.
     * @param mxWorld the world to unload
     * @param save whether to save the world
     * @return true if unloaded successfully
     */
    public boolean unloadMxWorld(MxWorld mxWorld, boolean save) {
        if (!mxWorld.isLoaded())
            return true;
        MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXATLAS, "Unloading MxWorld: " + mxWorld.getName());
        World w = Bukkit.getWorld(mxWorld.getWorldUID());
        if (w == null) {
            MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.MXATLAS, "Could not unload MxWorld (World is null): " + mxWorld.getName());
            return false;
        }
        for (Player p : w.getPlayers()) {
            p.teleport(MxFunctions.getSpawnLocation());
        }
        boolean unloaded = Bukkit.unloadWorld(w, save);
        if (unloaded) {
            mxWorld.setLoaded(false);
        } else {
            MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.MXATLAS, "Could not unload MxWorld: " + mxWorld.getName());
        }
        return unloaded;
    }

    /**
     * Deletes an {@link MxWorld} from disk.
     * @param mxWorld the world to delete
     * @return true if deleted successfully
     */
    public boolean deleteMxWorld(MxWorld mxWorld) {
        if (mxWorld.isLoaded()) {
            if (!unloadMxWorld(mxWorld, false)) {
                return false;
            }
        }

        try {
            FileUtils.deleteDirectory(mxWorld.getDir());
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.MXATLAS, "Could not delete MxWorld: " + mxWorld.getName());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Duplicates an {@link MxWorld} to a new directory.
     * @param worldToClone the world to clone
     * @param dir the parent directory for the clone
     * @return an {@link Optional} containing the new world if successful
     */
    public Optional<MxWorld> duplicateMxWorld(MxWorld worldToClone, File dir) {
        UUID uuid = UUID.randomUUID();

        File directoryToCloneTo = new File(dir + File.separator + uuid);
        try {
            FileUtils.copyDirectory(worldToClone.getDir(), directoryToCloneTo);
            File uidDat = new File(directoryToCloneTo.getAbsoluteFile() + File.separator + "uid.dat");
            uidDat.delete();

        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.MXATLAS, "Could not duplicate MxWorld: " + worldToClone.getName());
            e.printStackTrace();
            return Optional.empty();
        }
        MxWorld mxWorld = new MxWorld(uuid.toString(), uuid.toString(), directoryToCloneTo);
        worlds.add(mxWorld);

        return Optional.of(mxWorld);
    }

    /**
     * Unloads all tracked worlds and saves them.
     */
    public void unloadAll() {
        worlds.forEach(w -> {
            unloadMxWorld(w, true);
        });
    }

    /**
     * Scans a directory for folders containing {@code uid.dat} and loads them as {@link MxWorld}s.
     * @param dir the directory to scan
     * @return a list of loaded worlds
     */
    public List<MxWorld> loadFolder(File dir) {
        List<MxWorld> list = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                File uidDat = new File(file.getAbsolutePath() + "/uid.dat");
                if (uidDat.exists()) {
                    MxWorld mxWorld = new MxWorld(file.getName(), file.getName(), file);
                    list.add(mxWorld);
                    worlds.add(mxWorld);
                    MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXATLAS, "Adding world to MxAtlas: " + file.getName() + " (" + file.getAbsolutePath() + ")");
                } else {
                    MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXATLAS, "Could not load folder because it does not have a uid.dat file. (" + file.getAbsolutePath() + ")");
                }
            }
        }
        return list;
    }

    /**
     * Loads a directory as an {@link MxWorld}.
     * @param dir the world directory
     * @return an {@link Optional} containing the loaded world
     */
    public Optional<MxWorld> loadWorld(File dir) {
        MxWorld mxWorld = new MxWorld(dir.getName(), dir.getName(), dir);
        worlds.add(mxWorld);
        MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXATLAS, "Adding world to MxAtlas: " + dir.getName() + " (" + dir.getAbsolutePath() + ")");
        return Optional.of(mxWorld);
    }
}
