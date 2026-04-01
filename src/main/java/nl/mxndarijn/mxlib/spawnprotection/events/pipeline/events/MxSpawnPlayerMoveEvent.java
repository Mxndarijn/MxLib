package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Fired when a player moves in any non-game world.
 *
 * <p>The underlying {@link PlayerMoveEvent} is exposed so handlers can teleport the player
 * or otherwise react to movement (e.g. void rescue).</p>
 */
public final class MxSpawnPlayerMoveEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerMoveEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerMoveEvent}.
     *
     * @param player     the player who moved; must not be {@code null}
     * @param paperEvent the underlying Bukkit player move event; must not be {@code null}
     */
    public MxSpawnPlayerMoveEvent(Player player, PlayerMoveEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who moved.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit player move event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerMoveEvent getPaperEvent() {
        return paperEvent;
    }
}




