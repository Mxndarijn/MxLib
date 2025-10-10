package nl.mxndarijn.mxlib.configfiles;

import lombok.Getter;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@Getter
public final class ConfigHandle {
    private final ConfigFileType type;
    private final File file;
    private final FileConfiguration cfg;

    public ConfigHandle(ConfigFileType type, File file) {
        this.type = type;
        this.file = file;
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            Logger.logMessage(LogLevel.DEBUG, StandardPrefix.CONFIG_FILES,
                    "Saving file... " + type.path());
            cfg.save(file);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.CONFIG_FILES,
                    "Could not save file... " + type.path());
        }
    }

}
