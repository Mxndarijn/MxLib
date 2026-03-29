package nl.mxndarijn.mxlib.configfiles;

import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.util.MxFunctions;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MxConfigService {

    private static MxConfigService instance;

    private final JavaPlugin plugin;
    private final Map<String, MxConfigHandle> byPath = new ConcurrentHashMap<>();
    private final Set<MxConfigFileType> registeredTypes = new HashSet<>();

    private MxConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) instance = new MxConfigService(plugin);
        else MxLogger.logMessage(MxLogLevel.WARNING, MxStandardPrefix.CONFIG_FILES,
                "MxConfigService is al geïnitialiseerd.");
    }

    public static MxConfigService getInstance() {
        if (instance == null) throw new IllegalStateException("MxConfigService not initialized.");
        return instance;
    }

    /** Registreer in bulk alle enum-waarden van een enum die MxConfigFileType implementeert. */
    public <E extends Enum<E> & MxConfigFileType> void registerAll(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) register(e);
    }

    /** Registreer één type (kan ook uit andere modules komen). */
    public void register(MxConfigFileType type) {
        if (registeredTypes.add(type)) {
            ensureLoaded(type);
        }
    }

    /** Haal een handle op (toegang tot FileConfiguration, save, etc.). */
    public MxConfigHandle get(MxConfigFileType type) {
        ensureLoaded(type);
        return byPath.get(type.path());
    }

    /** Sla alle autoSave-bestanden op. */
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
