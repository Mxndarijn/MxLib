package nl.mxndarijn.mxlib.changeworld;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Interface for handling players entering, leaving, or quitting worlds.
 */
public interface MxChangeWorld {

    /**
     * Called when a player enters a world.
     * @param p the player
     * @param w the world
     * @param e the original Bukkit event
     */
    void enter(Player p, World w, PlayerChangedWorldEvent e);

    /**
     * Called when a player leaves a world.
     * @param p the player
     * @param w the world
     * @param e the original Bukkit event
     */
    void leave(Player p, World w, PlayerChangedWorldEvent e);

    /**
     * Called when a player quits while in a world.
     * @param p the player
     * @param w the world
     * @param e the original Bukkit event
     */
    void quit(Player p, World w, PlayerQuitEvent e);
}
