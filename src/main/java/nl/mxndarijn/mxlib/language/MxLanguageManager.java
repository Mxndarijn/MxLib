package nl.mxndarijn.mxlib.language;

import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.chatprefix.MxChatPrefixType;
import nl.mxndarijn.mxlib.configfiles.MxConfigService;
import nl.mxndarijn.mxlib.configfiles.MxStandardConfigFile;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.util.MxFunctions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public final class MxLanguageManager {

    private static MxLanguageManager instance;

    /** Active language configuration (selected file on disk + merged defaults). */
    private final FileConfiguration languageConfig;

    /** Physical file backing the active language configuration. */
    private final File languageFile;

    private MxLanguageManager() {
        final JavaPlugin plugin = MxLib.getPlugin();

        // Resolve configured language file (e.g., "languages/nl-NL.yml" or just "nl-NL.yml")
        final String configuredPath = MxConfigService.getInstance()
                .get(MxStandardConfigFile.MAIN_CONFIG)
                .getCfg()
                .getString("language-file", MxStandardConfigFile.DEFAULT_LANGUAGE.fileName());

        // Normalize to <dataFolder>/languages/<file>
        final String fileName = configuredPath.contains("/")
                ? configuredPath.substring(configuredPath.lastIndexOf('/') + 1)
                : configuredPath;

        final File languagesDir = new File(plugin.getDataFolder(), "languages");
        if (!languagesDir.exists() && !languagesDir.mkdirs()) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Could not create languages directory: " + languagesDir.getAbsolutePath());
        }

        File candidate = new File(languagesDir, fileName);

        // Ensure the configured language file exists on disk; copy from JAR if present
        if (!candidate.exists()) {
            // Important: resource path should include "languages/"
            MxFunctions.copyFileFromResources("languages/" + fileName, "languages/" + fileName);
        }

        // Fallback to default language file if still missing
        if (!candidate.exists()) {
            MxLogger.logMessage(MxLogLevel.FATAL, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Could not load language file '" + configuredPath + "'. Falling back to default '"
                            + MxStandardConfigFile.DEFAULT_LANGUAGE.fileName() + "'.");
            candidate = new File(languagesDir, MxStandardConfigFile.DEFAULT_LANGUAGE.fileName());
            if (!candidate.exists()) {
                MxFunctions.copyFileFromResources("languages/" + MxStandardConfigFile.DEFAULT_LANGUAGE.fileName(),
                        "languages/" + MxStandardConfigFile.DEFAULT_LANGUAGE.fileName());
            }
        }

        this.languageFile = candidate;
        this.languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Merge defaults from embedded selected + embedded base default
        mergeDefaults(plugin, fileName, MxStandardConfigFile.DEFAULT_LANGUAGE.fileName());

        MxLogger.logMessage(MxLogLevel.INFORMATION, MxStandardPrefix.LANGUAGE_MANAGER,
                languageConfig.getString("language-name", fileName) + " has been loaded! (" + fileName + ")");
    }

    public static MxLanguageManager getInstance() {
        if (instance == null) {
            instance = new MxLanguageManager();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Public API (names preserved)
    // -------------------------------------------------------------------------

    /**
     * Returns the localized string for the given key with positional placeholders (%%1%%, %%2%%, ...).
     * Ensures availability by backfilling from embedded defaults when missing.
     */
    public String getLanguageString(MxLanguageKey key, List<String> placeholders) {
        final String path = key.key();
        String text = ensureAvailability(path); // now returns the actual ensured string

        if ("LANGUAGE_NOT_FOUND".equals(text)) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Language entry not found for key: '" + path + "'");
            text = "<gray>LANGUAGE_NOT_FOUND (" + path + ")";
        }


        // apply placeholders
        if (placeholders != null && !placeholders.isEmpty()) {
            for (int i = 0; i < placeholders.size(); i++) {
                text = text.replace("%%" + (i + 1) + "%%", placeholders.get(i));
            }
        }
        return text;
    }


    /** Convenience overload without placeholders. */
    public String getLanguageString(MxLanguageKey key) {
        return getLanguageString(key, Collections.emptyList());
    }

    /** Convenience overload with prefix. */
    public String getLanguageString(MxLanguageKey key, List<String> placeholders, MxChatPrefixType prefix) {
        return String.valueOf(prefix) + getLanguageString(key, placeholders);
    }

    /** Convenience overload with prefix and no placeholders. */
    public String getLanguageString(MxLanguageKey key, MxChatPrefixType prefix) {
        return String.valueOf(prefix) + getLanguageString(key, Collections.emptyList());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Ensures a key exists in the active file; if missing, populate from embedded defaults or mark as not found. */
    private String ensureAvailability(String path) {
        if (languageConfig.contains(path)) {
            return languageConfig.getString(path);
        }

        final JavaPlugin plugin = MxLib.getPlugin();
        final String defaultRes = "languages/" + MxStandardConfigFile.DEFAULT_LANGUAGE.fileName();

        FileConfiguration defaults = loadResourceYaml(plugin, defaultRes);
        String value = defaults != null ? defaults.getString(path) : null;

        if (value == null) {
            value = "LANGUAGE_NOT_FOUND";
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.LANGUAGE_MANAGER,
                    path + " has no default value; please add it to the language resources.");
        }

        languageConfig.set(path, value);
        languageConfig.addDefault(path, value);
        languageConfig.options().copyDefaults(true);
        try {
            languageConfig.save(languageFile);
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Could not save language file: " + e.getMessage());
        }

        return value;
    }


    /** Merges defaults from selected and base default resource files into the active configuration. */
    private void mergeDefaults(JavaPlugin plugin, String selectedFileName, String defaultFileName) {
        int before = languageConfig.getKeys(true).size();

        FileConfiguration selDefaults = loadResourceYaml(plugin, "languages/" + selectedFileName);
        if (selDefaults != null) {
            languageConfig.addDefaults(selDefaults);
        }

        FileConfiguration baseDefaults = loadResourceYaml(plugin, "languages/" + defaultFileName);
        if (baseDefaults != null) {
            languageConfig.addDefaults(baseDefaults);
        } else {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Base default language resource not found: languages/" + defaultFileName);
        }

        languageConfig.options().copyDefaults(true);
        try {
            languageConfig.save(languageFile);
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Error while ensuring language defaults: " + e.getMessage());
        }

        int added = Math.max(0, languageConfig.getKeys(true).size() - before);
        if (added > 0) {
            MxLogger.logMessage(MxLogLevel.INFORMATION, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Added " + added + " missing language entries from defaults (" + selectedFileName + ")");
        }
    }

    /** Safe loader for embedded YAML; returns null if resource is missing. */
    private static FileConfiguration loadResourceYaml(JavaPlugin plugin, String resPath) {
        InputStream in = plugin.getResource(resPath);
        if (in == null) return null;
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.LANGUAGE_MANAGER,
                    "Failed reading embedded resource: " + resPath + " -> " + e.getMessage());
            return null;
        }
    }
}
