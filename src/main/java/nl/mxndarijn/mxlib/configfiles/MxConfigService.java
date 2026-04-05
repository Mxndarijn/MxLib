package nl.mxndarijn.mxlib.configfiles;

import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.util.MxFunctions;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing configuration files.
 * Handles registration, loading from resources, and saving of {@link MxConfigHandle}s.
 */
public final class MxConfigService {

    private static MxConfigService instance;

    private final JavaPlugin plugin;
    private final Map<String, MxConfigHandle> byPath = new ConcurrentHashMap<>();
    private final Set<MxConfigFileType> registeredTypes = new HashSet<>();

    private MxConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the singleton instance.
     * @param plugin the {@link JavaPlugin} instance
     */
    public static void init(JavaPlugin plugin) {
        if (instance == null) instance = new MxConfigService(plugin);
        else MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.CONFIG_FILES,
                "MxConfigService is already initialized.");
    }

    /**
     * Gets the singleton instance.
     * @return the {@code MxConfigService} instance
     * @throws IllegalStateException if not initialized
     */
    public static MxConfigService getInstance() {
        if (instance == null) throw new IllegalStateException("MxConfigService not initialized.");
        return instance;
    }

    /**
     * Registers all enum constants of an enum implementing {@link MxConfigFileType}.
     * @param <E> the enum type
     * @param enumClass the enum class
     */
    public <E extends Enum<E> & MxConfigFileType> void registerAll(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) register(e);
    }

    /**
     * Registers a single configuration file type.
     * @param type the config file type
     */
    public void register(MxConfigFileType type) {
        if (registeredTypes.add(type)) {
            ensureLoaded(type);
        }
    }

    /**
     * Retrieves the {@link MxConfigHandle} for a given type.
     * @param type the config file type
     * @return the config handle
     */
    public MxConfigHandle get(MxConfigFileType type) {
        ensureLoaded(type);
        return byPath.get(type.path());
    }

    /**
     * Saves all registered configuration files that have {@code autoSave()} enabled.
     */
    public void saveAll() {
        MxLogger.logMessage(MxLogLevel.INFORMATION, MxStandardPrefix.CONFIG_FILES, "Saving all files...");
        for (MxConfigFileType type : registeredTypes) {
            if (type.autoSave()) get(type).save();
        }
    }

    // ————— intern —————
    private void ensureLoaded(MxConfigFileType type) {
        byPath.computeIfAbsent(type.path(), p -> {
            File target = new File(plugin.getDataFolder(), p);
            if (!target.exists()) {
                MxLogger.logMessage(MxLogLevel.INFORMATION, MxStandardPrefix.CONFIG_FILES,
                        "Could not load: " + target.getName() + ". Trying to load it from internal sources...");
                // Kopieer resource -> bestand (maakt mappen aan indien nodig)
                MxFunctions.copyFileFromResources(type.fileName(), p);
            }
            return new MxConfigHandle(type, target);
        });
    }
}
