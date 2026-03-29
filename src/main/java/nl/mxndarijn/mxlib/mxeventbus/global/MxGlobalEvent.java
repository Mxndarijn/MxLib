package nl.mxndarijn.mxlib.mxeventbus.global;

import nl.mxndarijn.mxlib.mxeventbus.core.MxBaseEvent;
import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldType;

/**
 * Base class for all events dispatched through the {@link MxGlobalEventBus}.
 *
 * <p>Each event captures the world type of the world in which it occurred,
 * allowing the {@code @MxWorldTypes} guard to filter handlers by world category.</p>
 *
 * @param <W> the world-type enum that categorises worlds for this pipeline
 */
public abstract class MxGlobalEvent<W extends MxIWorldType> implements MxBaseEvent {

    private final W worldType;

    /**
     * Constructs a new global event with the given world type.
     *
     * @param worldType the category of the world in which the event occurred; must not be {@code null}
     */
    protected MxGlobalEvent(W worldType) {
        if (worldType == null) throw new IllegalArgumentException("worldType must not be null");
        this.worldType = worldType;
    }

    /**
     * Returns the world type captured when this event was created.
     *
     * @return the world type; never {@code null}
     */
    public W worldType() {
        return worldType;
    }
}






