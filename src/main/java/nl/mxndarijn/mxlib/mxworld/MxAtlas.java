package nl.mxndarijn.mxlib.mxworld;

import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.util.Functions;
import nl.mxndarijn.mxlib.util.VoidGenerator;
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

public class MxAtlas {
    private static JavaPlugin plugin;
    private static MxAtlas instance;
    private final ArrayList<MxWorld> worlds;

    public static void setPlugin(JavaPlugin plugin) {
        MxAtlas.plugin = plugin;
    }

    private MxAtlas(JavaPlugin plugin) {
        Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.MXATLAS, "Started MxAtlas... (World-Manager)");
        worlds = new ArrayList<>();
        setPlugin(plugin);
    }

    public static MxAtlas getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MxAtlas is not initialized!");
        }
        return instance;
    }

    public static void init(JavaPlugin plugin) {
        if(instance != null) {
            throw new IllegalStateException("MxAtlas is already initialized!");
        }
        else {
            instance = new MxAtlas(plugin);
        }
    }

    public Optional<MxWorld> getMxWorld(String name) {
        for (MxWorld w : worlds) {
            if (w.getName().equals(name)) {
                return Optional.of(w);
            }
        }
        return Optional.empty();
    }

    public Optional<MxWorld> getMxWorld(UUID uuid) {
        for (MxWorld w : worlds) {
            if (w.getUUID().equalsIgnoreCase(uuid.toString())) {
                return Optional.of(w);
            }
        }
        return Optional.empty();
    }

    public boolean addMxWorld(MxWorld world) {
        return worlds.add(world);
    }

    public boolean removeMxWorld(MxWorld world) {
        return worlds.remove(world);
    }

    public CompletableFuture<Boolean> loadMxWorld(MxWorld mxWorld) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Logger.logMessage(LogLevel.DEBUG, StandardPrefix.MXATLAS, "Loading MxWorld: " + mxWorld.getName());
        if (mxWorld.isLoaded()) {
            Logger.logMessage(LogLevel.WARNING, StandardPrefix.MXATLAS, mxWorld.getName() + " is already loaded.");
            future.complete(true);
            return future;
        }
        String path = mxWorld.getDir().toString().replace("\\", "/");
        WorldCreator wc = new WorldCreator(path);
        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generator(new VoidGenerator());
        wc.generateStructures(false);
        File dir = mxWorld.getDir();
        try {
            Logger.logMessage(LogLevel.DEBUG, StandardPrefix.MXATLAS,
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
            Logger.logMessage(LogLevel.DEBUG, StandardPrefix.MXATLAS, "Loading worldsettings.yml... ");
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

            GameRuleUtil.applyGameRules(world, worldSettings.getGameRules());

            if (worldSettings.getSpawn() != null) {
                Logger.logMessage(LogLevel.DEBUG, StandardPrefix.MXATLAS, "Setting spawnlocation... ");
                world.setSpawnLocation(Functions.getLocationFromConfiguration(world, worldSettings.getSpawn()));
            }

            mxWorld.setWorldUID(world.getUID());
            mxWorld.setLoaded(true);

            future.complete(true);
        });

        return future;
    }


    public boolean unloadMxWorld(MxWorld mxWorld, boolean save) {
        if (!mxWorld.isLoaded())
            return true;
        Logger.logMessage(LogLevel.DEBUG, StandardPrefix.MXATLAS, "Unloading MxWorld: " + mxWorld.getName());
        World w = Bukkit.getWorld(mxWorld.getWorldUID());
        if (w == null) {
            Logger.logMessage(LogLevel.WARNING, StandardPrefix.MXATLAS, "Could not unload MxWorld (World is null): " + mxWorld.getName());
            return false;
        }
        for (Player p : w.getPlayers()) {
            p.teleport(Functions.getSpawnLocation());
        }
        boolean unloaded = Bukkit.unloadWorld(w, save);
        if (unloaded) {
            mxWorld.setLoaded(false);
        } else {
            Logger.logMessage(LogLevel.WARNING, StandardPrefix.MXATLAS, "Could not unload MxWorld: " + mxWorld.getName());
        }
        return unloaded;
    }

    public boolean deleteMxWorld(MxWorld mxWorld) {
        if (mxWorld.isLoaded()) {
            if (!unloadMxWorld(mxWorld, false)) {
                return false;
            }
        }

        try {
            FileUtils.deleteDirectory(mxWorld.getDir());
        } catch (IOException e) {
            Logger.logMessage(LogLevel.WARNING, StandardPrefix.MXATLAS, "Could not delete MxWorld: " + mxWorld.getName());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<MxWorld> duplicateMxWorld(MxWorld worldToClone, File dir) {
        UUID uuid = UUID.randomUUID();

        File directoryToCloneTo = new File(dir + File.separator + uuid);
        try {
            FileUtils.copyDirectory(worldToClone.getDir(), directoryToCloneTo);
            File uidDat = new File(directoryToCloneTo.getAbsoluteFile() + File.separator + "uid.dat");
            uidDat.delete();

        } catch (IOException e) {
            Logger.logMessage(LogLevel.WARNING, StandardPrefix.MXATLAS, "Could not duplicate MxWorld: " + worldToClone.getName());
            e.printStackTrace();
            return Optional.empty();
        }
        MxWorld mxWorld = new MxWorld(uuid.toString(), uuid.toString(), directoryToCloneTo);
        worlds.add(mxWorld);

        return Optional.of(mxWorld);
    }

    public void unloadAll() {
        worlds.forEach(w -> {
            unloadMxWorld(w, true);
        });
    }

    public List<MxWorld> loadFolder(File dir) {
        List<MxWorld> list = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                File uidDat = new File(file.getAbsolutePath() + "/uid.dat");
                if (uidDat.exists()) {
                    MxWorld mxWorld = new MxWorld(file.getName(), file.getName(), file);
                    list.add(mxWorld);
                    worlds.add(mxWorld);
                    Logger.logMessage(LogLevel.DEBUG, StandardPrefix.MXATLAS, "Adding world to MxAtlas: " + file.getName() + " (" + file.getAbsolutePath() + ")");
                } else {
                    Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXATLAS, "Could not load folder because it does not have a uid.dat file. (" + file.getAbsolutePath() + ")");
                }
            }
        }
        return list;
    }

    public Optional<MxWorld> loadWorld(File dir) {
        MxWorld mxWorld = new MxWorld(dir.getName(), dir.getName(), dir);
        worlds.add(mxWorld);
        Logger.logMessage(LogLevel.DEBUG, StandardPrefix.MXATLAS, "Adding world to MxAtlas: " + dir.getName() + " (" + dir.getAbsolutePath() + ")");
        return Optional.of(mxWorld);
    }
}
