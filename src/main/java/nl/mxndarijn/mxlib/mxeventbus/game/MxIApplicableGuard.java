package nl.mxndarijn.mxlib.mxeventbus.game;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Strategy interface that resolves the set of allowed actor roles for a handler method.
 *
 * <p>Implementations read a guard annotation (e.g. {@code @Applicable}) from the method
 * and return the set of permitted {@link MxIApplicableTo} values. If no annotation is present,
 * implementations should return all known roles so the handler runs for every actor type.</p>
 *
 * @param <A> the concrete actor-role type, which must implement {@link MxIApplicableTo}
 */
public interface MxIApplicableGuard<A extends MxIApplicableTo> {

    /**
     * Resolves the set of allowed actor roles for the given handler method.
     *
     * @param method the handler method to inspect; must not be {@code null}
     * @return the set of permitted roles; never {@code null} or empty (return all roles when unrestricted)
     */
    Set<A> resolve(Method method);
}

