package nl.mxndarijn.mxlib.configfiles;

import nl.mxndarijn.mxlib.configfiles.ConfigFileType;
import nl.mxndarijn.mxlib.configfiles.ConfigHandle;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.util.Functions;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigService {

    private static ConfigService instance;

    private final JavaPlugin plugin;
    private final Map<String, ConfigHandle> byPath = new ConcurrentHashMap<>();
    private final Set<ConfigFileType> registeredTypes = new HashSet<>();

    private ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) instance = new ConfigService(plugin);
        else Logger.logMessage(LogLevel.WARNING, StandardPrefix.CONFIG_FILES,
                "ConfigService is al geïnitialiseerd.");
    }

    public static ConfigService getInstance() {
        if (instance == null) throw new IllegalStateException("ConfigService not initialized.");
        return instance;
    }

    /** Registreer in bulk alle enum-waarden van een enum die ConfigFileType implementeert. */
    public <E extends Enum<E> & ConfigFileType> void registerAll(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) register(e);
    }

    /** Registreer één type (kan ook uit andere modules komen). */
    public void register(ConfigFileType type) {
        if (registeredTypes.add(type)) {
            ensureLoaded(type);
        }
    }

    /** Haal een handle op (toegang tot FileConfiguration, save, etc.). */
    public ConfigHandle get(ConfigFileType type) {
        ensureLoaded(type);
        return byPath.get(type.path());
    }

    /** Sla alle autoSave-bestanden op. */
    public void saveAll() {
        Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.CONFIG_FILES, "Saving all files...");
        for (ConfigFileType type : registeredTypes) {
            if (type.autoSave()) get(type).save();
        }
    }

    // ————— intern —————
    private void ensureLoaded(ConfigFileType type) {
        byPath.computeIfAbsent(type.path(), p -> {
            File target = new File(plugin.getDataFolder(), p);
            if (!target.exists()) {
                Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.CONFIG_FILES,
                        "Could not load: " + target.getName() + ". Trying to load it from internal sources...");
                // Kopieer resource -> bestand (maakt mappen aan indien nodig)
                Functions.copyFileFromResources(type.fileName(), p);
            }
            return new ConfigHandle(type, target);
        });
    }
}
