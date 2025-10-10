package nl.mxndarijn.mxlib.inventory.heads;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.configfiles.ConfigService;
import nl.mxndarijn.mxlib.configfiles.StandardConfigFile;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class MxHeadManager {
    private static MxHeadManager instance;
    private final FileConfiguration fileConfiguration;

    public MxHeadManager() {
        fileConfiguration = ConfigService.getInstance().get(StandardConfigFile.HEAD_DATA).getCfg();
        // Ensure all default head-data keys from resources are present in the plugin's head-data file
        try {
            InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(MxLib.getPlugin().getResource(ConfigService.getInstance().get(StandardConfigFile.HEAD_DATA).getType().fileName())));
            FileConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
            int before = fileConfiguration.getKeys(false).size();
            fileConfiguration.addDefaults(defaults);
            fileConfiguration.options().copyDefaults(true);
            fileConfiguration.save(ConfigService.getInstance().get(StandardConfigFile.HEAD_DATA).getFile());
            int after = fileConfiguration.getKeys(false).size();
            int added = Math.max(0, after - before);
            if (added > 0) {
                Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.MXHEAD_MANAGER, "Added " + added + " missing head-data entries from defaults.");
            }
        } catch (Exception e) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Error while ensuring head-data defaults: " + e.getMessage());
        }
        long period = 30L * 60L * 20L; // 30 minutes in ticks
        long delay = 200L; // 10 seconds initial delay
        Bukkit.getScheduler().runTaskTimerAsynchronously(MxLib.getPlugin(), () -> {
            try {
                Logger.logMessage(LogLevel.INFORMATION, StandardPrefix.MXHEAD_MANAGER, "Refreshing up to 40 player skulls older than 2 days (least recently refreshed first)...");
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
                        Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not get texture for " + key + ", skipping texture...");
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
                Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Error during scheduled skull refresh: " + e.getMessage());
                e.printStackTrace();
            }
        }, delay, period);
    }

    public static MxHeadManager getInstance() {
        if (instance == null) {
            instance = new MxHeadManager();
        }
        return instance;
    }

    public Optional<String> getTextureValue(String name) {
        Optional<MxHeadSection> optionalSection = MxHeadSection.loadHead(name);
        if (optionalSection.isPresent()) {
            MxHeadSection section = optionalSection.get();
            return section.getValueOptional();
        }
        return Optional.empty();
    }

    public List<String> getAllHeadKeys() {
        return new ArrayList<>(fileConfiguration.getKeys(false));
    }

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
                            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not create MxHeadSection, wrong input.");
                            return false;
                        }
                        section.get().apply();
                        return true;
                    } else {
                        Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Error while creating head-data, no owner but type is player.");
                        return false;
                    }
                } else {
                    Optional<MxHeadSection> section = MxHeadSection.create(textureName, displayName, type, texture);
                    if (section.isEmpty()) {
                        Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not create MxHeadSection, wrong input.");
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
                    Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not find texture");
                    return Optional.empty();
                }
            } catch (Exception e) {
                Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Error while retrieving texture:");
                e.printStackTrace();
                return Optional.empty();
            }
        } else {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not load head data, because item was not a head.");
            return Optional.empty();
        }
    }

    private Optional<UUID> getOwner(ItemStack itemStack) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            OfflinePlayer player = skullMeta.getOwningPlayer();
            if (player == null) {
                Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not load head data, because it does not have an owner.");
                return Optional.empty();
            }
            return Optional.of(skullMeta.getOwningPlayer().getUniqueId());
        } else {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not load head data, because item was not a head.");
            return Optional.empty();
        }
    }

    public void removeHead(String key) {
        if (fileConfiguration.contains(key)) {
            fileConfiguration.set(key, null);
        }
    }

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
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXHEAD_MANAGER, "Could not retrieve skin data from mojang servers... (" + uuid.toString() + ")");
            e.printStackTrace();
        }
        return texture;
    }
}
