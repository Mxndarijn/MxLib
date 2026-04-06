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
     * Applies the full spawn-enter state to the given player.
     *
     * <p>A typical implementation should:
     * <ol>
     *   <li>Close and clear the player's inventory ({@code player.closeInventory(); player.getInventory().clear()})</li>
     *   <li>Set gamemode to ADVENTURE ({@code player.setGameMode(GameMode.ADVENTURE)})</li>
     *   <li>Disable glowing ({@code player.setGlowing(false)})</li>
     *   <li>Reset max health to default ({@link #resetMaxHealth(Player)})</li>
     *   <li>Restore food level ({@code player.setFoodLevel(20)})</li>
     *   <li>Disable flight ({@code player.setAllowFlight(false); player.setFlying(false)})</li>
     *   <li>Un-vanish the player for all others ({@link #showPlayerForAll(Player)})</li>
     *   <li>Remove all active potion effects</li>
     *   <li>Give spawn hotbar items ({@link #giveSpawnItems(Player)})</li>
     *   <li>Assign the spawn scoreboard ({@link #setPlayerScoreboard(UUID, MxSupplierScoreBoard)})</li>
     * </ol>
     *
     * @param player the player entering the spawn world; must not be {@code null}
     */
    void applySpawnEnterState(Player player);

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

    /**
     * Applies the full spawn-leave state to the given player.
     *
     * <p>A typical implementation should:
     * <ol>
     *   <li>Close and clear the player's inventory ({@code player.closeInventory(); player.getInventory().clear()})</li>
     *   <li>Remove all active potion effects</li>
     *   <li>Remove the spawn scoreboard ({@link #removePlayerScoreboard(UUID, MxSupplierScoreBoard)})</li>
     * </ol>
     *
     * @param player the player leaving the spawn world; must not be {@code null}
     */
    void applySpawnLeaveState(Player player);
}

