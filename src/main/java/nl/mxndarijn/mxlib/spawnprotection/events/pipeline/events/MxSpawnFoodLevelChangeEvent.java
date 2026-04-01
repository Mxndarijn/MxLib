package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * Fired when an entity's food level changes in any non-game world.
 *
 * <p>The underlying {@link FoodLevelChangeEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnFoodLevelChangeEvent extends MxGlobalEvent<MxWorldType> {

    private final FoodLevelChangeEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnFoodLevelChangeEvent}.
     *
     * @param paperEvent the underlying Bukkit food level change event; must not be {@code null}
     */
    public MxSpawnFoodLevelChangeEvent(FoodLevelChangeEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(paperEvent.getEntity().getWorld()));
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the underlying Bukkit food level change event.
     *
     * @return the paper event; never {@code null}
     */
    public FoodLevelChangeEvent getPaperEvent() {
        return paperEvent;
    }
}



