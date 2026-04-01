package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Fired when an entity is damaged by another entity in any non-game world.
 *
 * <p>The underlying {@link EntityDamageByEntityEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnEntityDamageByEntityEvent extends MxGlobalEvent<MxWorldType> {

    private final EntityDamageByEntityEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnEntityDamageByEntityEvent}.
     *
     * @param paperEvent the underlying Bukkit entity damage by entity event; must not be {@code null}
     */
    public MxSpawnEntityDamageByEntityEvent(EntityDamageByEntityEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(paperEvent.getEntity().getWorld()));
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the underlying Bukkit entity damage by entity event.
     *
     * @return the paper event; never {@code null}
     */
    public EntityDamageByEntityEvent getPaperEvent() {
        return paperEvent;
    }
}



