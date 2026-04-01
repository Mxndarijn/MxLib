package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * Fired when an entity changes a block in any non-game world (e.g. farmland trampling).
 *
 * <p>The underlying {@link EntityChangeBlockEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnEntityChangeBlockEvent extends MxGlobalEvent<MxWorldType> {

    private final EntityChangeBlockEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnEntityChangeBlockEvent}.
     *
     * @param paperEvent the underlying Bukkit entity change block event; must not be {@code null}
     */
    public MxSpawnEntityChangeBlockEvent(EntityChangeBlockEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(paperEvent.getBlock().getWorld()));
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the underlying Bukkit entity change block event.
     *
     * @return the paper event; never {@code null}
     */
    public EntityChangeBlockEvent getPaperEvent() {
        return paperEvent;
    }
}



