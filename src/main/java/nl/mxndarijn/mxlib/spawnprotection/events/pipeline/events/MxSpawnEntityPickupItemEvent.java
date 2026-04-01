package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.event.entity.EntityPickupItemEvent;

/**
 * Fired when an entity picks up an item in any non-game world.
 *
 * <p>The underlying {@link EntityPickupItemEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnEntityPickupItemEvent extends MxGlobalEvent<MxWorldType> {

    private final EntityPickupItemEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnEntityPickupItemEvent}.
     *
     * @param paperEvent the underlying Bukkit entity pickup item event; must not be {@code null}
     */
    public MxSpawnEntityPickupItemEvent(EntityPickupItemEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(paperEvent.getEntity().getWorld()));
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the underlying Bukkit entity pickup item event.
     *
     * @return the paper event; never {@code null}
     */
    public EntityPickupItemEvent getPaperEvent() {
        return paperEvent;
    }
}



