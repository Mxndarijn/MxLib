package nl.mxndarijn.mxlib.mxeventbus.game;

import java.util.Optional;
import java.util.UUID;

/**
 * Strategy interface that resolves the actor role for a given UUID within a game context.
 *
 * <p>Implementations map a UUID (or {@code null} for non-player actors) to the
 * appropriate {@link MxIApplicableTo} role. If the UUID does not belong to any
 * known actor in the current game, {@link Optional#empty()} should be returned
 * so the event bus can skip the handler.</p>
 *
 * @param <A> the concrete actor-role type, which must implement {@link MxIApplicableTo}
 */
public interface MxIActorResolver<A extends MxIApplicableTo> {

    /**
     * Resolves the actor role for the given UUID.
     *
     * @param uuid the UUID of the actor, or {@code null} for non-player (global/entity) actors
     * @return the resolved role, or {@link Optional#empty()} if the UUID is not a known actor
     */
    Optional<A> resolve(UUID uuid);
}

