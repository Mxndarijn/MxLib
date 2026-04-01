package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Fired when a player opens an inventory in any non-game world.
 *
 * <p>The underlying {@link InventoryOpenEvent} is exposed so handlers can react to it.</p>
 */
public final class MxSpawnInventoryOpenEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final InventoryOpenEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnInventoryOpenEvent}.
     *
     * @param player     the player opening the inventory; must not be {@code null}
     * @param paperEvent the underlying Bukkit inventory open event; must not be {@code null}
     */
    public MxSpawnInventoryOpenEvent(Player player, InventoryOpenEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who opened the inventory.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit inventory open event.
     *
     * @return the paper event; never {@code null}
     */
    public InventoryOpenEvent getPaperEvent() {
        return paperEvent;
    }
}




