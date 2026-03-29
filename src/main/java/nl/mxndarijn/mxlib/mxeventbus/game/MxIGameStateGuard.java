package nl.mxndarijn.mxlib.mxeventbus.game;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Strategy interface that resolves the set of allowed game states for a handler method.
 *
 * <p>Implementations read a guard annotation (e.g. {@code @States}) from the method
 * and return the set of permitted {@link MxIGameState} values. If no annotation is present,
 * implementations should return all known states so the handler runs unconditionally.</p>
 *
 * @param <S> the concrete game-state type, which must implement {@link MxIGameState}
 */
public interface MxIGameStateGuard<S extends MxIGameState> {

    /**
     * Resolves the set of allowed game states for the given handler method.
     *
     * @param method the handler method to inspect; must not be {@code null}
     * @return the set of permitted states; never {@code null} or empty (return all states when unrestricted)
     */
    Set<S> resolve(Method method);
}

