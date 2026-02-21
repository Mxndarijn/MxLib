package nl.mxndarijn.mxlib.mxitem;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Wrapper around {@link PlayerInteractEvent} that carries its own cancellation flag,
 * independent of the Bukkit event's cancelled state.
 * This allows subscribers to cancel the MxItem pipeline even when the underlying
 * Bukkit event was already cancelled (e.g. right-clicking air).
 */
public class MxPlayerInteractEvent {

    private final PlayerInteractEvent bukkitEvent;
    private final Player player;
    private boolean cancelled;

    public MxPlayerInteractEvent(PlayerInteractEvent bukkitEvent, Player player) {
        this.bukkitEvent = bukkitEvent;
        this.player = player;
        this.cancelled = false;
    }

    public PlayerInteractEvent getBukkitEvent() {
        return bukkitEvent;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
