package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Fired when an entity takes damage in any non-game world.
 *
 * <p>The underlying {@link EntityDamageEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnEntityDamageEvent extends MxGlobalEvent<MxWorldType> {

    private final EntityDamageEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnEntityDamageEvent}.
     *
     * @param paperEvent the underlying Bukkit entity damage event; must not be {@code null}
     */
    public MxSpawnEntityDamageEvent(EntityDamageEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(paperEvent.getEntity().getWorld()));
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the underlying Bukkit entity damage event.
     *
     * @return the paper event; never {@code null}
     */
    public EntityDamageEvent getPaperEvent() {
        return paperEvent;
    }
}



