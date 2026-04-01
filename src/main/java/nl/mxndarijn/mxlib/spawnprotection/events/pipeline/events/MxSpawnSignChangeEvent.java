package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Fired when a player edits a sign in any non-game world.
 *
 * <p>The underlying {@link SignChangeEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnSignChangeEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final SignChangeEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnSignChangeEvent}.
     *
     * @param player     the player who edited the sign; must not be {@code null}
     * @param paperEvent the underlying Bukkit sign change event; must not be {@code null}
     */
    public MxSpawnSignChangeEvent(Player player, SignChangeEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who edited the sign.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit sign change event.
     *
     * @return the paper event; never {@code null}
     */
    public SignChangeEvent getPaperEvent() {
        return paperEvent;
    }
}




