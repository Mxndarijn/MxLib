package nl.mxndarijn.mxlib.mxeventbus.core;

/**
 * Shared base for handler entries stored in an {@link MxAbstractEventBus}.
 *
 * <p>Holds the fields common to all concrete handler entry types:
 * name, priority, ignoreCancelled, the raw handler reference, and the owning listener.</p>
 *
 * @param <E> the event type this handler accepts
 * @param <H> the concrete handler functional interface type
 */
public abstract class MxBaseHandlerEntry<E extends MxBaseEvent, H> {

    /** Human-readable name used in trace output. */
    public final String name;

    /** Execution priority; lower ordinal runs first. */
    public final MxPriority priority;

    /** When {@code true}, this handler is skipped if the event is cancelled. */
    public final boolean ignoreCancelled;

    /** The actual handler logic invoked by the bus. */
    public final H handler;

    /**
     * The listener object that owns this handler. Used by
     * {@link MxAbstractEventBus#unregisterListener(Object)} to remove all handlers
     * belonging to a specific listener instance.
     */
    public final Object listener;

    /**
     * Constructs a new {@code MxBaseHandlerEntry}.
     *
     * @param name            human-readable name for trace output; must not be {@code null}
     * @param priority        execution priority; must not be {@code null}
     * @param ignoreCancelled when {@code true}, skip this handler if the event is cancelled
     * @param handler         the handler logic; must not be {@code null}
     * @param listener        the object that owns this handler; may be {@code null}
     */
    protected MxBaseHandlerEntry(String name,
                                MxPriority priority,
                                boolean ignoreCancelled,
                                H handler,
                                Object listener) {
        this.name = name;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
        this.handler = handler;
        this.listener = listener;
    }
}


