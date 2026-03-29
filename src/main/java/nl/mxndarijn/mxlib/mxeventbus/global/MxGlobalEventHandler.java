package nl.mxndarijn.mxlib.mxeventbus.global;

import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldType;

/**
 * Functional interface for handler logic invoked by the {@link MxGlobalEventBus}.
 *
 * @param <T> the event type this handler accepts
 * @param <W> the world-type enum used by the event
 */
@FunctionalInterface
public interface MxGlobalEventHandler<T extends MxGlobalEvent<W>, W extends MxIWorldType> {

    /**
     * Handles the given event context.
     *
     * @param ctx the event context; never {@code null}
     * @throws Exception if the handler throws any exception
     */
    void handle(MxGlobalEventContext<T, W> ctx) throws Exception;
}





