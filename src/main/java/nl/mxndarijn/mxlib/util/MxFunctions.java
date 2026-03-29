package nl.mxndarijn.mxlib.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class MxFunctions {

    /**
     * Returns the spawn location of the primary world (first world loaded by the server).
     *
     * @return the spawn location of the first world
     */
    public static Location getSpawnLocation() {
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    /**
     * Constructs a {@link Location} from a YAML {@link ConfigurationSection} containing
     * x, y, z and optionally yaw and pitch keys.
     *
     * @param w       the world the location belongs to
     * @param section the configuration section with coordinate values
     * @return the constructed location
     */
    public static Location getLocationFromConfiguration(World w, ConfigurationSection section) {
        if (section.contains("yaw") && section.contains("pitch")) {
            return new Location(w, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
        } else {
            return new Location(w, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
        }
    }

    /**
     * Copies a resource file from the plugin jar to the plugin data folder at the given relative path.
     *
     * @param fileName the resource file name inside the jar
     * @param path     the relative destination path inside the plugin data folder
     */
    public static void copyFileFromResources(String fileName, String path) {
        JavaPlugin plugin = MxLib.getPlugin();
        File destFile = new File(plugin.getDataFolder() + File.separator + path);
        destFile.getParentFile().mkdirs();
        copyFileFromResources(fileName, destFile);
    }

    /**
     * Copies a resource file from the plugin jar to the given destination file.
     *
     * @param fileName the resource file name inside the jar
     * @param destFile the destination file to write to
     */
    public static void copyFileFromResources(String fileName, File destFile) {
        destFile.getParentFile().mkdirs();
        JavaPlugin plugin = MxLib.getPlugin();
        InputStream inputStream = plugin.getResource(fileName);
        if (inputStream == null) {
            MxLogger.logMessage(MxLogLevel.FATAL, MxStandardPrefix.CONFIG_FILES, "Could not load resource: " + fileName);
            return;
        }
        try (inputStream) {
            Files.copy(inputStream, destFile.toPath());
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.FATAL, MxStandardPrefix.CONFIG_FILES, "Could not create config file: " + fileName);
        }
    }

    /**
     * Serializes an Adventure {@link Component} to a plain text string (no formatting).
     *
     * @param c the component to serialize
     * @return the plain text representation
     */
    public static String convertComponentToString(Component c) {
        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.builder().build();
        return plainSerializer.serialize(c);
    }

    /**
     * Formats a duration in milliseconds as a {@code MM:SS} string.
     *
     * @param timeInMillis the duration in milliseconds
     * @return formatted time string, e.g. {@code "02:45"}
     */
    public static String formatGameTime(long timeInMillis) {
        long timeInSeconds = timeInMillis / 1000;
        long minutes = timeInSeconds / 60;
        long seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Deserializes a MiniMessage string into an Adventure {@link Component}
     * with italic formatting disabled by default.
     *
     * @param input the MiniMessage-formatted string
     * @return the deserialized component
     */
    public static Component buildComponentFromString(String input) {
        return MiniMessage.miniMessage().deserialize("<!i>" + input);
    }

    /**
     * Fills all blocks in the axis-aligned bounding box defined by two corners with the given material.
     *
     * @param corner1  one corner of the region
     * @param corner2  the opposite corner of the region
     * @param material the material to fill with
     */
    public static void fillBlock(Location corner1, Location corner2, Material material) {
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    corner1.getWorld().getBlockAt(x, y, z).setType(material);
                }
            }
        }
    }

    /**
     * Replaces all blocks of a given material within the axis-aligned bounding box
     * defined by two corners with a different material.
     *
     * @param corner1 one corner of the region
     * @param corner2 the opposite corner of the region
     * @param from    the material to replace
     * @param to      the material to replace with
     */
    public static void replaceBlock(Location corner1, Location corner2, Material from, Material to) {
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (corner1.getWorld().getBlockAt(x, y, z).getType() == from) {
                        corner1.getWorld().getBlockAt(x, y, z).setType(to);
                    }
                }
            }
        }
    }
}
