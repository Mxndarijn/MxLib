package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Fired when a player clicks inside an inventory in any non-game world.
 *
 * <p>The underlying {@link InventoryClickEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnInventoryClickEvent extends MxGlobalEvent<MxWorldType> {

    private final InventoryClickEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnInventoryClickEvent}.
     *
     * @param paperEvent the underlying Bukkit inventory click event; must not be {@code null}
     */
    public MxSpawnInventoryClickEvent(InventoryClickEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(paperEvent.getWhoClicked().getWorld()));
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the underlying Bukkit inventory click event.
     *
     * @return the paper event; never {@code null}
     */
    public InventoryClickEvent getPaperEvent() {
        return paperEvent;
    }
}



