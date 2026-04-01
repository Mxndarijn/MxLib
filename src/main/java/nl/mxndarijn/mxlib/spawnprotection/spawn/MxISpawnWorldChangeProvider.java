package nl.mxndarijn.mxlib.spawnprotection.spawn;

import nl.mxndarijn.mxlib.mxscoreboard.MxSupplierScoreBoard;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Provides world-change transition dependencies to {@link MxSpawnWorldChangeListener}.
 *
 * <p>Implement this interface with WIDM-specific logic (inventory items, permissions,
 * vanish, scoreboard management, player utilities) and inject it into
 * {@link MxSpawnWorldChangeListener} so that the listener itself contains no WIDM references.</p>
 */
public interface MxISpawnWorldChangeProvider {

    /**
     * Resets the player's max health to the default value.
     *
     * @param player the player whose max health should be reset; must not be {@code null}
     */
    void resetMaxHealth(Player player);

    /**
     * Makes the given player visible to all other players (un-vanishes them).
     *
     * @param player the player to show; must not be {@code null}
     */
    void showPlayerForAll(Player player);

    /**
     * Teleports the given player to the spawn location.
     *
     * @param player the player to teleport; must not be {@code null}
     */
    void teleportToSpawn(Player player);

    /**
     * Gives the player their spawn hotbar items based on their permissions.
     *
     * @param player the player to give items to; must not be {@code null}
     */
    void giveSpawnItems(Player player);

    /**
     * Retrieves the spawn scoreboard for the given player UUID, if one exists.
     *
     * @param uuid the player UUID; must not be {@code null}
     * @return the {@link MxSupplierScoreBoard} for the player, or {@code null} if none exists
     */
    MxSupplierScoreBoard getScoreboard(UUID uuid);

    /**
     * Sets the active scoreboard for the given player.
     *
     * @param uuid        the player UUID; must not be {@code null}
     * @param scoreboard  the scoreboard to assign; may be {@code null} to clear
     */
    void setPlayerScoreboard(UUID uuid, MxSupplierScoreBoard scoreboard);

    /**
     * Removes the active scoreboard for the given player.
     *
     * @param uuid        the player UUID; must not be {@code null}
     * @param scoreboard  the scoreboard to remove; may be {@code null}
     */
    void removePlayerScoreboard(UUID uuid, MxSupplierScoreBoard scoreboard);
}

