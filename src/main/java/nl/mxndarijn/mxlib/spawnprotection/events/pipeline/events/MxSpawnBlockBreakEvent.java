package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Fired when a player attempts to break a block in any non-game world.
 *
 * <p>The underlying {@link BlockBreakEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnBlockBreakEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final BlockBreakEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnBlockBreakEvent}.
     *
     * @param player     the player breaking the block; must not be {@code null}
     * @param paperEvent the underlying Bukkit block break event; must not be {@code null}
     */
    public MxSpawnBlockBreakEvent(Player player, BlockBreakEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who attempted to break the block.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit block break event.
     *
     * @return the paper event; never {@code null}
     */
    public BlockBreakEvent getPaperEvent() {
        return paperEvent;
    }
}




