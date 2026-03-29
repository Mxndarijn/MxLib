package nl.mxndarijn.mxlib.mxeventbus.game;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Strategy interface that resolves the set of permitted world types for a handler method.
 *
 * <p>Implementations inspect annotations (or any other metadata) on the given method and
 * return the set of {@link MxIWorldType} values for which the handler should run.
 * Returning an empty set means the handler is effectively disabled; returning all known
 * values (or {@code null}) means the handler runs for every world type.</p>
 *
 * <p>The canonical WIDM implementation reads the {@code @MxWorldTypes} annotation and
 * falls back to all values of the {@code MxWorldType} enum when the annotation is absent.</p>
 *
 * @param <W> the world-type token; must implement {@link MxIWorldType}
 */
public interface MxIWorldTypeGuard<W extends MxIWorldType> {

    /**
     * Resolves the set of world types for which the given handler method is permitted to run.
     *
     * @param method the handler method being registered; never {@code null}
     * @return a non-null {@link Set} of permitted world-type values;
     *         an empty set disables the handler, a full set enables it for all worlds
     */
    Set<W> resolve(Method method);
}

