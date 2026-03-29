package nl.mxndarijn.mxlib.mxeventbus.core;

import java.util.Set;

/**
 * Internal representation of a single registered event handler with its guards.
 *
 * <p>All fields are immutable after construction.</p>
 *
 * @param <T> the event type this handler accepts
 */
public final class MxHandlerEntry<T extends MxBaseEvent> extends MxBaseHandlerEntry<T, MxEventHandler<T>> {

    /**
     * Allowed states. When non-empty, the handler is skipped if the event's state
     * is not contained in this set.
     */
    public final Set<?> allowedStates;

    /**
     * Allowed actor roles. When non-empty, the handler is skipped if the
     * actor's role is not contained in this set.
     */
    public final Set<?> allowedTargets;

    /**
     * Constructs a new {@code MxHandlerEntry}.
     *
     * @param name            human-readable name for trace output; must not be {@code null}
     * @param priority        execution priority; must not be {@code null}
     * @param allowedStates   set of permitted states; must not be {@code null}
     * @param allowedTargets  set of permitted actor roles; must not be {@code null}
     * @param ignoreCancelled when {@code true}, skip this handler if the event is denied
     * @param handler         the handler logic; must not be {@code null}
     * @param listener        the object that owns this handler; may be {@code null}
     */
    public MxHandlerEntry(String name,
                 MxPriority priority,
                 Set<?> allowedStates,
                 Set<?> allowedTargets,
                 boolean ignoreCancelled,
                 MxEventHandler<T> handler,
                 Object listener) {
        super(name, priority, ignoreCancelled, handler, listener);
        this.allowedStates = allowedStates;
        this.allowedTargets = allowedTargets;
    }
}

