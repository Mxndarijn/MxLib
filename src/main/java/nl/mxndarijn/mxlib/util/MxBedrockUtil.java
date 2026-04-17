package nl.mxndarijn.mxlib.util;

import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for detecting Bedrock players via the Floodgate API (preferred)
 * or the Geyser API as a fallback when Floodgate is not installed.
 */
public class MxBedrockUtil {

    /**
     * Returns whether the Floodgate plugin is currently loaded and its API is available.
     *
     * @return {@code true} if Floodgate is present and enabled, {@code false} otherwise
     */
    public static boolean isFloodgateAvailable() {
        if (Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            return false;
        }
        try {
            return FloodgateApi.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns whether the Geyser plugin is currently loaded and its API is available.
     *
     * @return {@code true} if Geyser is present and enabled, {@code false} otherwise
     */
    public static boolean isGeyserAvailable() {
        if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") == null
                && Bukkit.getPluginManager().getPlugin("Geyser-Paper") == null) {
            return false;
        }
        try {
            return GeyserApi.api() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns whether the player with the given UUID is a Bedrock player.
     * <p>
     * Uses Floodgate if available, otherwise falls back to the Geyser API.
     * Returns {@code false} if neither is available.
     *
     * @param uuid the UUID of the player to check
     * @return {@code true} if the player is a Bedrock player, {@code false} otherwise
     */
    public static boolean isBedrockPlayer(UUID uuid) {
        if (isFloodgateAvailable()) {
            try {
                return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
            } catch (Exception e) {
                return false;
            }
        }
        if (isGeyserAvailable()) {
            try {
                return GeyserApi.api().connectionByUuid(uuid) != null;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Returns the XUID (Xbox User ID) of the Bedrock player with the given UUID.
     * <p>
     * Uses Floodgate if available, otherwise falls back to the Geyser API.
     * Returns {@link Optional#empty()} if neither is available or the player is not a Bedrock player.
     *
     * @param uuid the UUID of the player
     * @return an {@link Optional} containing the XUID string, or empty if not available
     */
    public static Optional<String> getXuid(UUID uuid) {
        if (isFloodgateAvailable()) {
            try {
                org.geysermc.floodgate.api.player.FloodgatePlayer player = FloodgateApi.getInstance().getPlayer(uuid);
                if (player != null) {
                    return Optional.of(player.getXuid());
                }
            } catch (Exception e) {
                // fall through to Geyser
            }
        }
        if (isGeyserAvailable()) {
            try {
                GeyserConnection connection = GeyserApi.api().connectionByUuid(uuid);
                if (connection != null) {
                    return Optional.of(connection.xuid());
                }
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
