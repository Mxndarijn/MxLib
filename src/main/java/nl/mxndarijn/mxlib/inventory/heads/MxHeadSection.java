package nl.mxndarijn.mxlib.inventory.heads;

import lombok.Getter;
import lombok.Setter;
import nl.mxndarijn.mxlib.configfiles.ConfigService;
import nl.mxndarijn.mxlib.configfiles.StandardConfigFile;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a head entry in the HEAD_DATA config file.
 * Fields are nullable; Optional is used only for return types (factories/getters) per modern Java best practices.
 */
@Setter
public class MxHeadSection {

    // Core data (nullable by design)
    private String value;
    private UUID uuid;
    private MxHeadsType type;
    private String name;
    private LocalDateTime lastRefreshed;

    // Config key (section name inside HEAD_DATA)
    @Getter
    private String key;

    private MxHeadSection() {
        // Use static factories
    }

    // ----------------------
    // Static factory methods
    // ----------------------

    /**
     * Creates a section with all fields present.
     * Returns Optional.empty() when the data is invalid.
     */
    public static Optional<MxHeadSection> create(String textureName,
                                                 String displayName,
                                                 MxHeadsType type,
                                                 String texture,
                                                 UUID uuid) {
        MxHeadSection mx = new MxHeadSection();
        mx.key = textureName;
        mx.name = displayName;
        mx.type = type;
        mx.value = texture;
        mx.uuid = uuid;
        return mx.validate() ? Optional.of(mx) : Optional.empty();
    }

    /**
     * Creates a section without UUID (useful for non-player types).
     * Returns Optional.empty() when the data is invalid.
     */
    public static Optional<MxHeadSection> create(String textureName,
                                                 String displayName,
                                                 MxHeadsType type,
                                                 String texture) {
        return create(textureName, displayName, type, texture, null);
    }

    /**
     * Loads a head section by key from HEAD_DATA.
     * Returns Optional.empty() when missing or invalid.
     */
    public static Optional<MxHeadSection> loadHead(String key) {
        FileConfiguration cfg = ConfigService.getInstance()
                .get(StandardConfigFile.HEAD_DATA)
                .getCfg();

        ConfigurationSection section = cfg.getConfigurationSection(key);
        if (section == null) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not load head: " + key);
            return Optional.empty();
        }

        MxHeadSection mx = new MxHeadSection();
        mx.key = key;

        mx.name = section.getString("name");
        mx.value = section.getString("value");

        // Parse type using your helper (assumes Optional return)
        mx.type = MxHeadsType.getTypeFromName(section.getString("type", null)).orElse(null);

        // UUID parsing (optional)
        String uuidStr = section.getString("uuid", null);
        if (uuidStr != null) {
            try {
                mx.uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
                mx.uuid = null;
            }
        }

        // Refreshed timestamp (optional)
        String refreshedStr = section.getString("refreshed", null);
        if (refreshedStr != null) {
            try {
                mx.lastRefreshed = LocalDateTime.parse(refreshedStr);
            } catch (Exception ignored) {
                mx.lastRefreshed = null;
            }
        }

        return mx.validate() ? Optional.of(mx) : Optional.empty();
    }

    // -------------
    // Mutators
    // -------------

    // ----------------------
    // Optional-style getters
    // ----------------------

    /** Optional view for nullable uuid. */
    public Optional<UUID> getUuidOptional() {
        return Optional.ofNullable(uuid);
    }


    public Optional<MxHeadsType> getTypeOptional() {
        return Optional.ofNullable(type);
    }

    public Optional<String> getValueOptional() {
        return Optional.ofNullable(value);
    }
    public Optional<String> getNameOptional() {
        return Optional.ofNullable(name);
    }

    /** Optional view for nullable lastRefreshed. */
    public Optional<LocalDateTime> getLastRefreshedOptional() {
        return Optional.ofNullable(lastRefreshed);
    }

    // -------------
    // Validation
    // -------------

    /**
     * Validates whether the instance has the required fields:
     * - name != null
     * - value != null
     * - type != null
     * - if type == PLAYER, then uuid != null
     */
    public boolean validate() {
        return name != null
                && value != null
                && type != null
                && (type != MxHeadsType.PLAYER || uuid != null);
    }

    // -------------
    // Persistence
    // -------------

    /**
     * Writes this head section back to the HEAD_DATA config.
     * Performs a validation check before saving.
     */
    public void apply() {
        if (!validate()) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER,
                    "Could not save MxHeadSection " + key + " because it was not valid.");
            return;
        }

        FileConfiguration cfg = ConfigService.getInstance()
                .get(StandardConfigFile.HEAD_DATA)
                .getCfg();

        ConfigurationSection section = cfg.getConfigurationSection(key);
        if (section == null) {
            section = cfg.createSection(key);
        }

        section.set("name", name);
        section.set("value", value);
        section.set("type", type.getType());
        section.set("uuid", uuid != null ? uuid.toString() : null);
        section.set("refreshed", lastRefreshed != null ? lastRefreshed.toString() : null);
    }
}
