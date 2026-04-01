package nl.mxndarijn.mxlib.spawnprotection.spawn;

import nl.mxndarijn.mxlib.mxscoreboard.MxSupplierScoreBoard;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Provides player lifecycle dependencies to {@link MxSpawnPlayerLifecycleListener}.
 *
 * <p>Implement this interface with WIDM-specific logic (scoreboard creation, game queue
 * management, game-world checks) and inject it into {@link MxSpawnPlayerLifecycleListener}
 * so that the listener itself contains no WIDM references.</p>
 */
public interface MxISpawnLifecycleProvider {

    /**
     * Creates and starts a spawn scoreboard for the given player.
     *
     * @param player the player to create the scoreboard for; must not be {@code null}
     * @return the created and started {@link MxSupplierScoreBoard}; never {@code null}
     */
    MxSupplierScoreBoard createScoreboard(Player player);

    /**
     * Returns whether the player with the given UUID is currently in a game.
     *
     * @param uuid the player UUID to check; must not be {@code null}
     * @return {@code true} if the player is in a game
     */
    boolean isPlayerInGame(UUID uuid);

    /**
     * Teleports the given player to the spawn location.
     *
     * @param player the player to teleport; must not be {@code null}
     */
    void teleportToSpawn(Player player);

    /**
     * Removes the given player from all game queues they are currently in.
     *
     * @param uuid the UUID of the player to remove from queues; must not be {@code null}
     */
    void removePlayerFromAllQueues(UUID uuid);

    /**
     * Fires a {@link org.bukkit.event.player.PlayerChangedWorldEvent} for the given player.
     *
     * @param player   the player who changed worlds; must not be {@code null}
     * @param from     the world the player came from; must not be {@code null}
     */
    void callPlayerChangedWorldEvent(Player player, World from);

    World getSpawnWorld();
}

