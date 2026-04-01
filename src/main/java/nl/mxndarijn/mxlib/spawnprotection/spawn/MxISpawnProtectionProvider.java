package nl.mxndarijn.mxlib.spawnprotection.spawn;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Provides spawn-world protection rules to {@link MxSpawnProtectionListener}.
 *
 * <p>Implement this interface with WIDM-specific logic (permissions, manager singletons,
 * block-set configuration) and inject it into {@link MxSpawnProtectionListener} so that
 * the listener itself contains no WIDM references.</p>
 */
public interface MxISpawnProtectionProvider {

    /**
     * Returns whether the given player currently has spawn-modify mode active.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player is in modify mode
     */
    boolean isInModifyMode(Player player);

    /**
     * Teleports the given player to the spawn location.
     *
     * @param player the player to teleport; must not be {@code null}
     */
    void teleportToSpawn(Player player);

    /**
     * Schedules a task to run on the next server tick.
     *
     * @param task the task to run; must not be {@code null}
     */
    void runTask(Runnable task);

    /**
     * Returns whether the given player has permission to drop items in the spawn world.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player has the drop-item permission
     */
    boolean hasDropItemPermission(Player player);

    /**
     * Returns whether the given entity has permission to pick up items in the spawn world.
     *
     * @param entity the entity to check; must not be {@code null}
     * @return {@code true} if the entity has the pickup-item permission
     */
    boolean hasPickupItemPermission(org.bukkit.entity.Entity entity);

    /**
     * Returns whether the given player has permission to change their inventory in the spawn world.
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player has the change-inventory permission
     */
    boolean hasChangeInventoryPermission(Player player);

    /**
     * Returns the set of block materials that open an inventory when interacted with
     * (e.g. chests, barrels) and should be blocked in the spawn world.
     *
     * @return an unmodifiable set of inventory-opening block materials; never {@code null}
     */
    Set<Material> getInventoryOpeningBlocks();

    /**
     * Returns the set of block materials that can be opened/closed (gates, doors, trapdoors)
     * and should trigger a scheduled state restore in the spawn world.
     *
     * @return an unmodifiable set of openable block materials; never {@code null}
     */
    Set<Material> getOpenableBlocks();
}

