package nl.mxndarijn.mxlib.mxworld;

import nl.mxndarijn.mxlib.util.MxFunctions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MxWorldSettings {

    public enum Key {
        AUTOSAVE("autosave"),
        SPAWN_CHUNKS("spawn_chunks"),
        SPAWN_CHUNKS_FORCE_LOADED_RADIUS("spawn_chunks.force_loaded_radius_chunks"),
        GAMERULES("gamerules"),
        SPAWN("spawn");

        private final String path;

        Key(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    private final boolean autoSaveEnabled;
    private final int spawnChunksForceLoadedRadius;
    private final ConfigurationSection gameRules;
    private final ConfigurationSection spawn;

    private MxWorldSettings(FileConfiguration cfg) {
        this.autoSaveEnabled = cfg.getBoolean(Key.AUTOSAVE.getPath(), false);
        this.spawnChunksForceLoadedRadius = cfg.getInt(Key.SPAWN_CHUNKS_FORCE_LOADED_RADIUS.getPath(), 0);
        this.gameRules = cfg.getConfigurationSection(Key.GAMERULES.getPath());
        this.spawn = cfg.getConfigurationSection(Key.SPAWN.getPath());
    }

    public static MxWorldSettings load(File worldDir) {
        File settingsFile = new File(worldDir, "worldsettings.yml");
        if (!settingsFile.exists()) {
            MxFunctions.copyFileFromResources("worldsettings.yml", settingsFile);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(settingsFile);
        return new MxWorldSettings(cfg);
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public int getSpawnChunksForceLoadedRadius() {
        return spawnChunksForceLoadedRadius;
    }

    public ConfigurationSection getGameRules() {
        return gameRules;
    }

    public ConfigurationSection getSpawn() {
        return spawn;
    }
}
