package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Fired when a player interacts with a block in any non-game world.
 *
 * <p>The underlying {@link PlayerInteractEvent} is exposed so handlers can cancel it
 * or schedule block-state restores for openable blocks.</p>
 */
public final class MxSpawnPlayerInteractEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerInteractEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerInteractEvent}.
     *
     * @param player     the player who interacted; must not be {@code null}
     * @param paperEvent the underlying Bukkit player interact event; must not be {@code null}
     */
    public MxSpawnPlayerInteractEvent(Player player, PlayerInteractEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who interacted.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit player interact event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerInteractEvent getPaperEvent() {
        return paperEvent;
    }
}




