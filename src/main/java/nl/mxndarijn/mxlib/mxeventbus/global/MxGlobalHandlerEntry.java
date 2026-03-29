package nl.mxndarijn.mxlib.mxeventbus.global;

import nl.mxndarijn.mxlib.mxeventbus.core.MxBaseHandlerEntry;
import nl.mxndarijn.mxlib.mxeventbus.core.MxPriority;
import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldType;

import java.util.Set;

/**
 * Internal representation of a single registered handler in the {@link MxGlobalEventBus}.
 *
 * <p>Extends {@link MxBaseHandlerEntry} with the global-pipeline-specific
 * world-type guard. Instances are created by {@link MxGlobalAnnotationBinder}
 * and stored in the bus registry. All fields are immutable after construction.</p>
 *
 * @param <T> the event type this handler accepts
 * @param <W> the world-type enum used by the event
 */
final class MxGlobalHandlerEntry<T extends MxGlobalEvent<W>, W extends MxIWorldType>
        extends MxBaseHandlerEntry<T, MxGlobalEventHandler<T, W>> {

    /**
     * Allowed world types. When non-empty, the handler is skipped if the event's
     * {@link MxGlobalEvent#worldType()} is not contained in this set.
     */
    final Set<W> allowedWorlds;

    /**
     * Constructs a new {@code MxGlobalHandlerEntry}.
     *
     * @param name            human-readable name for trace output; must not be {@code null}
     * @param priority        execution priority; must not be {@code null}
     * @param allowedWorlds   set of permitted world types; may be {@code null} or empty for all
     * @param ignoreCancelled when {@code true}, skip this handler if the event is cancelled
     * @param handler         the handler logic; must not be {@code null}
     * @param listener        the object that owns this handler; may be {@code null}
     */
    MxGlobalHandlerEntry(String name,
                       MxPriority priority,
                       Set<W> allowedWorlds,
                       boolean ignoreCancelled,
                       MxGlobalEventHandler<T, W> handler,
                       Object listener) {
        super(name, priority, ignoreCancelled, handler, listener);
        this.allowedWorlds = allowedWorlds;
    }
}






