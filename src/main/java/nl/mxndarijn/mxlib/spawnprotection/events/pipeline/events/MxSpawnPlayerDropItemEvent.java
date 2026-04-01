package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Fired when a player drops an item in any non-game world.
 *
 * <p>The underlying {@link PlayerDropItemEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnPlayerDropItemEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerDropItemEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerDropItemEvent}.
     *
     * @param player     the player dropping the item; must not be {@code null}
     * @param paperEvent the underlying Bukkit drop item event; must not be {@code null}
     */
    public MxSpawnPlayerDropItemEvent(Player player, PlayerDropItemEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who dropped the item.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit drop item event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerDropItemEvent getPaperEvent() {
        return paperEvent;
    }
}




