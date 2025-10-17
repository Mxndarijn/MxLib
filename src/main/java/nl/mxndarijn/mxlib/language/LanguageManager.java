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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public final class LanguageManager {

    private static LanguageManager instance;

    /** Active language configuration (selected file on disk + merged defaults). */
    private final FileConfiguration languageConfig;

    /** Physical file backing the active language configuration. */
    private final File languageFile;

    private LanguageManager() {
        final JavaPlugin plugin = MxLib.getPlugin();

        // Resolve configured language file (e.g., "languages/nl-NL.yml" or just "nl-NL.yml")
        final String configuredPath = ConfigService.getInstance()
                .get(StandardConfigFile.MAIN_CONFIG)
                .getCfg()
                .getString("language-file", StandardConfigFile.DEFAULT_LANGUAGE.fileName());

        // Normalize to <dataFolder>/languages/<file>
        final String fileName = configuredPath.contains("/")
                ? configuredPath.substring(configuredPath.lastIndexOf('/') + 1)
                : configuredPath;

        final File languagesDir = new File(plugin.getDataFolder(), "languages");
        if (!languagesDir.exists() && !languagesDir.mkdirs()) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Could not create languages directory: " + languagesDir.getAbsolutePath());
        }

        File candidate = new File(languagesDir, fileName);

        // Ensure the configured language file exists on disk; copy from JAR if present
        if (!candidate.exists()) {
            // Important: resource path should include "languages/"
            Functions.copyFileFromResources("languages/" + fileName, "languages/" + fileName);
        }

        // Fallback to default language file if still missing
        if (!candidate.exists()) {
            Logger.logMessage(LogLevel.FATAL, StandardPrefix.LANGUAGE_MANAGER,
                    "Could not load language file '" + configuredPath + "'. Falling back to default '"
                            + StandardConfigFile.DEFAULT_LANGUAGE.fileName() + "'.");
            candidate = new File(languagesDir, StandardConfigFile.DEFAULT_LANGUAGE.fileName());
            if (!candidate.exists()) {
                Functions.copyFileFromResources("languages/" + StandardConfigFile.DEFAULT_LANGUAGE.fileName(),
                        "languages/" + StandardConfigFile.DEFAULT_LANGUAGE.fileName());
            }
        }

        this.languageFile = candidate;
        this.languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        // Merge defaults from embedded selected + embedded base default
        mergeDefaults(plugin, fileName, StandardConfigFile.DEFAULT_LANGUAGE.fileName());

        Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.LANGUAGE_MANAGER,
                languageConfig.getString("language-name", fileName) + " has been loaded! (" + fileName + ")");
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
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
    public String getLanguageString(LanguageKey key, List<String> placeholders) {
        final String path = key.key();
        String text = ensureAvailability(path); // now returns the actual ensured string

        if ("LANGUAGE_NOT_FOUND".equals(text)) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Language entry not found for key: '" + path + "'");
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

    /** Ensures a key exists in the active file; if missing, populate from embedded defaults or mark as not found. */
    private String ensureAvailability(String path) {
        if (languageConfig.contains(path)) {
            return languageConfig.getString(path);
        }

        final JavaPlugin plugin = MxLib.getPlugin();
        final String defaultRes = "languages/" + StandardConfigFile.DEFAULT_LANGUAGE.fileName();

        FileConfiguration defaults = loadResourceYaml(plugin, defaultRes);
        String value = defaults != null ? defaults.getString(path) : null;

        if (value == null) {
            value = "LANGUAGE_NOT_FOUND";
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    path + " has no default value; please add it to the language resources.");
        }

        languageConfig.set(path, value);
        languageConfig.addDefault(path, value);
        languageConfig.options().copyDefaults(true);
        try {
            languageConfig.save(languageFile);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
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
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Base default language resource not found: languages/" + defaultFileName);
        }

        languageConfig.options().copyDefaults(true);
        try {
            languageConfig.save(languageFile);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Error while ensuring language defaults: " + e.getMessage());
        }

        int added = Math.max(0, languageConfig.getKeys(true).size() - before);
        if (added > 0) {
            Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.LANGUAGE_MANAGER,
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
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.LANGUAGE_MANAGER,
                    "Failed reading embedded resource: " + resPath + " -> " + e.getMessage());
            return null;
        }
    }
}
