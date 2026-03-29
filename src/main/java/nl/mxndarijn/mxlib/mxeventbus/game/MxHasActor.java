package nl.mxndarijn.mxlib.mxeventbus.game;

import java.util.Optional;
import java.util.UUID;

/**
 * Marker interface for game events that are associated with a specific actor.
 *
 * <p>The actor-role guard checks this interface to obtain the actor's UUID,
 * resolves it to an {@link MxIApplicableTo} role via an {@link MxIActorResolver},
 * and decides whether a handler should be invoked.</p>
 *
 * <p>Return {@link Optional#empty()} to indicate a global/non-actor event.</p>
 */
public interface MxHasActor {

    /**
     * Returns the UUID of the actor that triggered this event, or empty for global/non-actor events.
     *
     * @return the actor UUID, or {@link Optional#empty()} if there is no specific actor
     */
    Optional<UUID> getActor();
}

