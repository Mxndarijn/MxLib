package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * Fired when any entity (mob, minecart, boat, etc.) spawns in any non-game world.
 *
 * <p>The underlying {@link EntitySpawnEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnEntitySpawnEvent extends MxGlobalEvent<MxWorldType> {

    private final EntitySpawnEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnEntitySpawnEvent}.
     *
     * @param paperEvent the underlying Bukkit entity spawn event; must not be {@code null}
     */
    public MxSpawnEntitySpawnEvent(EntitySpawnEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(paperEvent.getEntity().getWorld()));
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the underlying Bukkit entity spawn event.
     *
     * @return the paper event; never {@code null}
     */
    public EntitySpawnEvent getPaperEvent() {
        return paperEvent;
    }
}

