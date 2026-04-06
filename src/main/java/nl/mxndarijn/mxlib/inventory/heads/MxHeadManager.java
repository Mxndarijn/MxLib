package nl.mxndarijn.mxlib.inventory.heads;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.configfiles.MxConfigService;
import nl.mxndarijn.mxlib.configfiles.MxStandardConfigFile;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Singleton manager for player and custom skull heads.
 * Handles storing, loading, and periodically refreshing head texture data
 * from the HEAD_DATA config file and the Mojang session server.
 */
public class MxHeadManager {
    private static MxHeadManager instance;
    private final FileConfiguration fileConfiguration;

    /**
     * Constructs a new {@code MxHeadManager} and schedules a periodic task
     * to refresh up to 40 player skull textures older than 2 days.
     */
    public MxHeadManager() {
        fileConfiguration = MxConfigService.getInstance().get(MxStandardConfigFile.HEAD_DATA).getCfg();
        long period = 30L * 60L * 20L; // 30 minutes in ticks
        long delay = 200L; // 10 seconds initial delay
        Bukkit.getScheduler().runTaskTimerAsynchronously(MxLib.getPlugin(), () -> {
            try {
                MxLogger.logMessage(MxLogLevel.INFORMATION, MxStandardPrefix.MXHEAD_MANAGER, "Refreshing up to 40 player skulls older than 2 days (least recently refreshed first)...");
                // Collect PLAYER heads with their lastRefreshed
                List<MxHeadSection> playerHeads = new ArrayList<>();
                for (String key : fileConfiguration.getKeys(false)) {
                    Optional<MxHeadSection> optionalSection = MxHeadSection.loadHead(key);
                    if (optionalSection.isPresent()) {
                        MxHeadSection section = optionalSection.get();
                        if (section.getTypeOptional().isPresent() && section.getTypeOptional().get() == MxHeadsType.PLAYER) {
                            playerHeads.add(section);
                        }
                    }
                }
                // Filter: only refresh if never refreshed or lastRefreshed older than 2 days
                LocalDateTime cutoff = LocalDateTime.now().minusDays(2);
                List<MxHeadSection> eligibleHeads = new ArrayList<>();
                for (MxHeadSection s : playerHeads) {
                    LocalDateTime lr = s.getLastRefreshedOptional().orElse(LocalDateTime.MIN);
                    if (!lr.isAfter(cutoff)) {
                        eligibleHeads.add(s);
                    }
                }
                // Sort eligible by lastRefreshed (null/empty treated as oldest)
                eligibleHeads.sort(Comparator.comparing(s -> s.getLastRefreshedOptional().orElse(LocalDateTime.MIN)));
                int toProcess = Math.min(40, eligibleHeads.size());
                for (int i = 0; i < toProcess; i++) {
                    MxHeadSection section = eligibleHeads.get(i);
                    String key = section.getKey();
                    if (section.getUuidOptional().isEmpty()) continue;
                    Optional<String> value = getTexture(section.getUuidOptional().get());
                    if (value.isEmpty()) {
                        MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not get texture for " + key + ", skipping texture...");
                        // Still mark as refreshed to avoid hot-looping failing entries
                        section.setLastRefreshed(LocalDateTime.now());
                        section.apply();
                        continue;
                    }
                    if (section.getValueOptional().isEmpty() || !section.getValueOptional().get().equalsIgnoreCase(value.get())) {
                        section.setValue(value.get());
                    }
                    section.setLastRefreshed(LocalDateTime.now());
                    section.apply();
                }
            } catch (Exception e) {
                MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Error during scheduled skull refresh: " + e.getMessage());
                e.printStackTrace();
            }
        }, delay, period);
    }

    /**
     * Returns the singleton instance, creating it if necessary.
     *
     * @return the {@code MxHeadManager} instance
     */
    public static MxHeadManager getInstance() {
        if (instance == null) {
            instance = new MxHeadManager();
        }
        return instance;
    }

    /**
     * Returns the stored texture value for the head with the given name.
     *
     * @param name the head key
     * @return an {@link Optional} containing the texture value, or empty if not found
     */
    public Optional<String> getTextureValue(String name) {
        Optional<MxHeadSection> optionalSection = MxHeadSection.loadHead(name);
        if (optionalSection.isPresent()) {
            MxHeadSection section = optionalSection.get();
            return section.getValueOptional();
        }
        return Optional.empty();
    }

    /**
     * Returns all head keys stored in the HEAD_DATA config.
     *
     * @return a list of all head keys
     */
    public List<String> getAllHeadKeys() {
        return new ArrayList<>(fileConfiguration.getKeys(false));
    }

    /**
     * Stores the skull texture from the given item stack under the specified name.
     *
     * @param itemStack   the skull item to extract the texture from
     * @param textureName the key to store the texture under
     * @param displayName the display name for the head entry
     * @param type        the head type ({@link MxHeadsType#PLAYER} or {@link MxHeadsType#MANUALLY_ADDED})
     * @return {@code true} if the texture was stored successfully, {@code false} otherwise
     */
    public boolean storeSkullTexture(ItemStack itemStack, String textureName, String displayName, MxHeadsType type) {
        Optional<MxHeadSection> optionalSection = MxHeadSection.loadHead(textureName);
        Optional<UUID> ownerOptional = Optional.empty();
        if (type == MxHeadsType.PLAYER) {
            ownerOptional = getOwner(itemStack);
        }
        Optional<String> optionalTexture = Optional.empty();
        if (type == MxHeadsType.PLAYER) {
            if (ownerOptional.isPresent()) {
                optionalTexture = getTexture(ownerOptional.get());
            }
        } else {
            optionalTexture = getTextureValue(itemStack);
        }
        if (optionalTexture.isPresent()) {
            String texture = optionalTexture.get();
            if (optionalSection.isPresent()) {
                MxHeadSection section = optionalSection.get();
                section.setType(type);
                section.setValue(texture);
                section.setName(displayName);
                ownerOptional.ifPresent(section::setUuid);
                section.apply();
                return true;
            } else {
                if (type == MxHeadsType.PLAYER) {
                    if (ownerOptional.isPresent()) {
                        Optional<MxHeadSection> section = MxHeadSection.create(textureName, displayName, type, texture, ownerOptional.get());
                        if (section.isEmpty()) {
                            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not create MxHeadSection, wrong input.");
                            return false;
                        }
                        section.get().apply();
                        return true;
                    } else {
                        MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Error while creating head-data, no owner but type is player.");
                        return false;
                    }
                } else {
                    Optional<MxHeadSection> section = MxHeadSection.create(textureName, displayName, type, texture);
                    if (section.isEmpty()) {
                        MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not create MxHeadSection, wrong input.");
                        return false;
                    }
                    section.get().apply();
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<String> getTextureValue(ItemStack itemStack) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            try {

                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                PlayerProfile profile = skullMeta.getPlayerProfile();

                Optional<ProfileProperty> optionalTexture = profile.getProperties().stream().findFirst();
                if (optionalTexture.isPresent()) {
                    ProfileProperty texture = optionalTexture.get();
                    return Optional.of(texture.getValue());
                } else {
                    MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not find texture");
                    return Optional.empty();
                }
            } catch (Exception e) {
                MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Error while retrieving texture:");
                e.printStackTrace();
                return Optional.empty();
            }
        } else {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not load head data, because item was not a head.");
            return Optional.empty();
        }
    }

    private Optional<UUID> getOwner(ItemStack itemStack) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            OfflinePlayer player = skullMeta.getOwningPlayer();
            if (player == null) {
                MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not load head data, because it does not have an owner.");
                return Optional.empty();
            }
            return Optional.of(skullMeta.getOwningPlayer().getUniqueId());
        } else {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not load head data, because item was not a head.");
            return Optional.empty();
        }
    }

    /**
     * Removes the head entry with the given key from the HEAD_DATA config.
     *
     * @param key the head key to remove
     */
    public void removeHead(String key) {
        if (fileConfiguration.contains(key)) {
            fileConfiguration.set(key, null);
        }
    }

    /**
     * Returns the {@link MxHeadSection} for the given key.
     *
     * @param key the head key
     * @return an {@link Optional} containing the section, or empty if not found
     */
    public Optional<MxHeadSection> getHeadSection(String key) {
        return MxHeadSection.loadHead(key);
    }

    private Optional<String> getTexture(UUID uuid) {
        Optional<String> texture = Optional.empty();
        try {
            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            texture = Optional.of(textureProperty.get("value").getAsString());
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXHEAD_MANAGER, "Could not retrieve skin data from mojang servers... (" + uuid.toString() + ")");
            e.printStackTrace();
        }
        return texture;
    }
}
