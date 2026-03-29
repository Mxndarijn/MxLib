package nl.mxndarijn.mxlib.mxeventbus.core;

/**
 * Functional interface for the handler logic invoked by an {@link MxAbstractEventBus}.
 *
 * <p>Implementations receive a {@link MxBaseContext} and may inspect the event,
 * submit verdicts, or apply side-effects.</p>
 *
 * @param <T> the event type this handler accepts
 */
@FunctionalInterface
public interface MxEventHandler<T extends MxBaseEvent> {
    /**
     * Handles the given event context.
     *
     * @param ctx the event context; never {@code null}
     * @throws Exception if the handler throws any exception
     */
    void handle(MxBaseContext<T> ctx) throws Exception;
}


