package nl.mxndarijn.mxlib.spawnprotection.spawn;

import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides chat-routing dependencies to {@link MxSpawnChatListener}.
 *
 * <p>Implement this interface with WIDM-specific logic (language strings, map lookups,
 * host preferences, game-world checks) and inject it into {@link MxSpawnChatListener} so
 * that the listener itself contains no WIDM references.</p>
 */
public interface MxISpawnChatProvider {

    /**
     * Returns whether the player with the given UUID is currently in a game
     * (including as a spectator).
     *
     * @param uuid the player UUID to check; must not be {@code null}
     * @return {@code true} if the player is in a game or spectating one
     */
    boolean isPlayerInGameWithSpectatorCheck(UUID uuid);

    /**
     * Builds the fully-formatted global chat {@link Component} for the given sender and message.
     *
     * @param sender  the player who sent the message; must not be {@code null}
     * @param message the raw chat message component; must not be {@code null}
     * @return the formatted global chat component; never {@code null}
     */
    Component buildGlobalChatMessage(Player sender, Component message);

    /**
     * Builds the fully-formatted private map chat {@link Component} for the given sender and message.
     *
     * @param sender  the player who sent the message; must not be {@code null}
     * @param message the raw chat message component; must not be {@code null}
     * @return the formatted map chat component; never {@code null}
     */
    Component buildMapChatMessage(Player sender, Component message);

    /**
     * Returns the map world for the given world, if the world is a map world.
     *
     * @param world the world to look up; must not be {@code null}
     * @return an {@link Optional} containing the map world's {@link UUID}, or empty if not a map world
     */
    Optional<UUID> getMapWorldUid(World world);

    /**
     * Returns whether the given player has private map chat enabled.
     *
     * @param uuid the player UUID to check; must not be {@code null}
     * @return {@code true} if private map chat is enabled for this player
     */
    boolean hasPrivateMapChat(UUID uuid);

    /**
     * Returns whether the given player is in a map world with private chat enabled,
     * and should therefore be excluded from global chat broadcasts.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player should be excluded from global broadcast
     */
    boolean shouldExcludeFromGlobalBroadcast(Player player);
}

