package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Fired when a player attempts to place a block in any non-game world.
 *
 * <p>The underlying {@link BlockPlaceEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnBlockPlaceEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final BlockPlaceEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnBlockPlaceEvent}.
     *
     * @param player     the player placing the block; must not be {@code null}
     * @param paperEvent the underlying Bukkit block place event; must not be {@code null}
     */
    public MxSpawnBlockPlaceEvent(Player player, BlockPlaceEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who attempted to place the block.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit block place event.
     *
     * @return the paper event; never {@code null}
     */
    public BlockPlaceEvent getPaperEvent() {
        return paperEvent;
    }
}




