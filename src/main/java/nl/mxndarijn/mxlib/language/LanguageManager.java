package nl.mxndarijn.mxlib.language;

import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.chatprefix.ChatPrefixType;
import nl.mxndarijn.mxlib.configfiles.ConfigService;
import nl.mxndarijn.mxlib.configfiles.StandardConfigFile;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.util.Functions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central language manager that:
 * 1) Loads and maintains the active language file and its defaults.
 * 2) Acts as a registry for language keys so modules can register their own keys (extensible).
 *
 * Use LanguageKey as the contract for keys. Core keys can live in an enum (e.g., StandardLanguageText implements LanguageKey).
 */
public final class LanguageManager {

    private static LanguageManager instance;

    /** Active language configuration (selected file + defaults merged). */
    private final FileConfiguration languageConfig;

    /** Physical file backing the active language configuration. */
    private final File languageFile;

    /** Registry of keys so modules can contribute their own sets. */
    private final Map<String, LanguageKey> registry = new ConcurrentHashMap<>();

    private LanguageManager() {
        final JavaPlugin plugin = MxLib.getPlugin();

        // Resolve configured language file path from MAIN_CONFIG (e.g., "languages/nl-NL.yml" or "nl-NL.yml")
        final String configuredPath = ConfigService.getInstance()
                .get(StandardConfigFile.MAIN_CONFIG)
                .getCfg()
                .getString("language-file", StandardConfigFile.DEFAULT_LANGUAGE.fileName());

        // Normalize to "languages/<file>" under the plugin data folder
        final String fileNameOnly = configuredPath.contains("/") ? configuredPath.substring(configuredPath.lastIndexOf('/') + 1) : configuredPath;
        final File languagesDir = new File(plugin.getDataFolder(), "languages");
        if (!languagesDir.exists()) {
            languagesDir.mkdirs();
        }
        File candidate = new File(languagesDir, fileNameOnly);

        // Ensure the configured language file exists, otherwise try to copy from resources
        if (!candidate.exists()) {
            Functions.copyFileFromResources(fileNameOnly, "languages/" + fileNameOnly);
        }
        // Fallback to default language file if still missing
        if (!candidate.exists()) {
            Logger.logMessage(LogLevel.FATAL, StandardPrefix.LANGUAGE_MANAGER,
                    "Could not load language file '" + configuredPath + "'... falling back to default '" + StandardConfigFile.DEFAULT_LANGUAGE.fileName() + "'");
            candidate = new File(languagesDir, StandardConfigFile.DEFAULT_LANGUAGE.fileName());
            if (!candidate.exists()) {
                // Ensure the default exists too
                Functions.copyFileFromResources(StandardConfigFile.DEFAULT_LANGUAGE.fileName(),
                        "languages/" + StandardConfigFile.DEFAULT_LANGUAGE.fileName());
            }
        }

        this.languageFile = candidate;
        this.languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Merge defaults from: (1) selected language resource (if present), (2) default language resource
        mergeDefaults(plugin, fileNameOnly, StandardConfigFile.DEFAULT_LANGUAGE.fileName());

        Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.LANGUAGE_MANAGER,
                languageConfig.getString("language-name", fileNameOnly) + " has been loaded! (" + fileNameOnly + ")");
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Registry API (extensible keys)
    // -------------------------------------------------------------------------

    /** Registers a single LanguageKey. Last write wins for duplicate key strings. */
    public void register(LanguageKey key) {
        if (key == null || key.key() == null) return;
        registry.put(key.key(), key);
    }

    /** Registers all enum constants from an enum that implements LanguageKey. */
    public <E extends Enum<E> & LanguageKey> void registerAll(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            register(e);
        }
    }

    /** Looks up a key object by its string. */
    public Optional<LanguageKey> find(String key) {
        return Optional.ofNullable(registry.get(key));
    }

    /** Returns an immutable view of all registered keys. */
    public Collection<LanguageKey> all() {
        return Collections.unmodifiableCollection(registry.values());
    }

    // -------------------------------------------------------------------------
    // Retrieval API
    // -------------------------------------------------------------------------

    /**
     * Returns the localized string for the given key with positional placeholders (%%1%%, %%2%%, ...).
     * Missing entries are added from defaults if possible; otherwise "LANGUAGE_NOT_FOUND" is inserted.
     */
    public String getLanguageString(LanguageKey key, List<String> placeholders) {
        ensureAvailability(key.key());
        String text = languageConfig.getString(key.key(), "LANGUAGE_NOT_FOUND");
        if (placeholders != null && !placeholders.isEmpty()) {
            for (int i = 0; i < placeholders.size(); i++) {
                text = text.replace("%%" + (i + 1) + "%%", placeholders.get(i));
            }
        }
        return text;
    }

    /** Convenience overload without placeholders. */
    public String getLanguageString(LanguageKey key) {
        return getLanguageString(key, Collections.emptyList());
    }

    /** Convenience overload with prefix. */
    public String getLanguageString(LanguageKey key, List<String> placeholders, ChatPrefixType prefix) {
        return String.valueOf(prefix) + getLanguageString(key, placeholders);
    }

    /** Convenience overload with prefix and no placeholders. */
    public String getLanguageString(LanguageKey key, ChatPrefixType prefix) {
        return String.valueOf(prefix) + getLanguageString(key, Collections.emptyList());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Ensures a key exists in the active file; if missing, attempts to populate from embedded defaults or marks as not found. */
    private void ensureAvailability(String key) {
        if (languageConfig.contains(key)) return;

        // Attempt to read from embedded default language resource
        final JavaPlugin plugin = MxLib.getPlugin();
        final String defaultRes = "languages/" + StandardConfigFile.DEFAULT_LANGUAGE.fileName();
        try (InputStream in = plugin.getResource(defaultRes);
             InputStreamReader reader = in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null) {

            String value = null;
            if (reader != null) {
                FileConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
                value = defaults.getString(key);
            }

            if (value == null) {
                value = "LANGUAGE_NOT_FOUND";
                Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                        key + " has no default value; please add it to the language resources.");
            }

            languageConfig.addDefault(key, value);
            languageConfig.options().copyDefaults(true);
            languageConfig.save(languageFile);

        } catch (Exception e) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Could not save or load default language file: " + e.getMessage());
        }
    }

    /** Merges defaults from selected and base default resource files into the active configuration. */
    private void mergeDefaults(JavaPlugin plugin, String selectedFileName, String defaultFileName) {
        int before = languageConfig.getKeys(true).size();

        // 1) Selected language defaults (if embedded)
        final String selectedRes = "languages/" + selectedFileName;
        try (InputStream in = plugin.getResource(selectedRes);
             InputStreamReader reader = in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null) {
            if (reader != null) {
                FileConfiguration selDefaults = YamlConfiguration.loadConfiguration(reader);
                languageConfig.addDefaults(selDefaults);
            }
        } catch (Exception ignored) {
            // If not embedded, skip silently
        }

        // 2) Base default language defaults (embedded)
        final String baseRes = "languages/" + defaultFileName;
        try (InputStream in = plugin.getResource(baseRes);
             InputStreamReader reader = in != null ? new InputStreamReader(in, StandardCharsets.UTF_8) : null) {
            if (reader != null) {
                FileConfiguration baseDefaults = YamlConfiguration.loadConfiguration(reader);
                languageConfig.addDefaults(baseDefaults);
            } else {
                Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                        "Base default language resource not found: " + baseRes);
            }
        } catch (Exception e) {
            File file = new File(plugin.getDataFolder(), baseRes);
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Could not load base default language from resources: " + e.getMessage() + " baseRes=" + baseRes + "  file=" + file.getAbsolutePath());
        }

        languageConfig.options().copyDefaults(true);
        try {
            languageConfig.save(languageFile);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Error while ensuring language defaults: " + e.getMessage());
        }

        int after = languageConfig.getKeys(true).size();
        int added = Math.max(0, after - before);
        if (added > 0) {
            Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.LANGUAGE_MANAGER,
                    "Added " + added + " missing language entries from defaults (" + selectedFileName + ")");
        }
    }
}
