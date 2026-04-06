package nl.mxndarijn.mxlib.spawnprotection.spawn;

import nl.mxndarijn.mxlib.changeworld.MxChangeWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player state transitions when entering or leaving the spawn world.
 *
 * <p>On enter: clears inventory, resets gamemode/health/food/flight/effects,
 * shows the player to all, teleports to spawn, and gives the appropriate hotbar items
 * and scoreboard. On leave: clears inventory, removes effects, and removes the scoreboard.</p>
 *
 * <p>All WIDM-specific logic (items, permissions, vanish, scoreboard management,
 * player utilities) is delegated to the injected {@link MxISpawnWorldChangeProvider}.</p>
 */
public final class MxSpawnWorldChangeListener implements MxChangeWorld {

    private final MxISpawnWorldChangeProvider provider;

    /**
     * Constructs a new {@code MxSpawnWorldChangeListener}.
     *
     * @param provider the {@link MxISpawnWorldChangeProvider} supplying world-change dependencies
     */
    public MxSpawnWorldChangeListener(MxISpawnWorldChangeProvider provider) {
        this.provider = provider;
    }

    /**
     * Resets the player's state and sets up the spawn experience when they enter the spawn world.
     *
     * @param p the player entering the spawn world
     * @param w the spawn world
     * @param e the world-change event that triggered this callback
     */
    @Override
    public void enter(Player p, World w, PlayerChangedWorldEvent e) {
        provider.applySpawnEnterState(p);
        provider.teleportToSpawn(p);
    }

    /**
     * Clears the player's inventory and removes their spawn scoreboard when they leave the spawn world.
     *
     * @param p the player leaving the spawn world
     * @param w the spawn world
     * @param e the world-change event that triggered this callback
     */
    @Override
    public void leave(Player p, World w, PlayerChangedWorldEvent e) {
        provider.applySpawnLeaveState(p);
    }

    /**
     * No action required when a player quits from the spawn world.
     *
     * @param p the player who quit
     * @param w the spawn world
     * @param e the quit event
     */
    @Override
    public void quit(Player p, World w, PlayerQuitEvent e) {
        // no action needed
    }
}

