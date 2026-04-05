package nl.mxndarijn.mxlib.configfiles;

import lombok.Getter;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Wrapper for a {@link FileConfiguration} and its corresponding {@link File}.
 */
@Getter
public final class MxConfigHandle {
    private final MxConfigFileType type;
    private final File file;
    private final FileConfiguration cfg;

    /**
     * Constructs a new {@code MxConfigHandle}.
     * @param type the config file type
     * @param file the physical file
     */
    public MxConfigHandle(MxConfigFileType type, File file) {
        this.type = type;
        this.file = file;
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves the configuration to disk.
     */
    public void save() {
        try {
            MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.CONFIG_FILES,
                    "Saving file... " + type.path());
            cfg.save(file);
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.CONFIG_FILES,
                    "Could not save file... " + type.path());
        }
    }

}
