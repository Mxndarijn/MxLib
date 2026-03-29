package nl.mxndarijn.mxlib.mxeventbus.global;

import nl.mxndarijn.mxlib.mxeventbus.core.MxAbstractAnnotationBinder;
import nl.mxndarijn.mxlib.mxeventbus.core.MxPriority;
import nl.mxndarijn.mxlib.mxeventbus.core.MxSubscribe;
import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldType;
import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldTypeGuard;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Scans an object for {@link MxSubscribe}-annotated methods and registers them into a
 * {@link MxGlobalEventBus}.
 *
 * <p><b>Method signature requirements</b></p>
 * <p>Each {@code @MxSubscribe} method must declare exactly one parameter typed as
 * {@code MxGlobalEventContext<T, W>} where {@code T} is a concrete subclass of
 * {@link MxGlobalEvent}, and must return {@code void}. Violations throw
 * {@link IllegalArgumentException} at bind time.</p>
 *
 * <p><b>Guard resolution</b></p>
 * <p>The set of permitted world types for each handler is determined by the
 * {@link MxIWorldTypeGuard} supplied at construction time. The canonical WIDM
 * implementation reads the {@code @MxWorldTypes} annotation and falls back to all
 * world types when the annotation is absent.</p>
 *
 * @param <W> the world-type token used by the bus and events
 */
public class MxGlobalAnnotationBinder<W extends MxIWorldType> extends MxAbstractAnnotationBinder<MxGlobalEventBus<W>> {

    /** Strategy that resolves the permitted world types for each handler method. */
    private final MxIWorldTypeGuard<W> worldTypeGuard;

    /**
     * Constructs a new {@code MxGlobalAnnotationBinder} with the given world-type guard.
     *
     * @param worldTypeGuard strategy that resolves permitted world types from a method; must not be {@code null}
     */
    public MxGlobalAnnotationBinder(MxIWorldTypeGuard<W> worldTypeGuard) {
        if (worldTypeGuard == null) throw new IllegalArgumentException("worldTypeGuard must not be null");
        this.worldTypeGuard = worldTypeGuard;
    }

    /**
     * Scans {@code listener} for {@link MxSubscribe}-annotated methods and registers them into {@code bus}.
     *
     * @param bus      the global event bus to register handlers into; must not be {@code null}
     * @param listener the object whose methods are scanned; must not be {@code null}
     * @throws IllegalArgumentException if any {@code @MxSubscribe} method has an invalid signature
     */
    public void bind(MxGlobalEventBus<W> bus, Object listener) {
        doBind(bus, listener);
    }

    /**
     * Removes all handlers registered by {@code listener} from {@code bus}.
     *
     * @param bus      the global event bus to remove handlers from; must not be {@code null}
     * @param listener the object whose handlers should be removed; must not be {@code null}
     */
    public void unbind(MxGlobalEventBus<W> bus, Object listener) {
        doUnbindAll(bus, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerHandler(MxGlobalEventBus<W> bus, Object listener, Method method) {
        String methodRef = listener.getClass().getSimpleName() + "#" + method.getName();

        // Validate and infer MxGlobalEventContext<T, W> parameter
        ParameterizedType pt = (ParameterizedType) method.getGenericParameterTypes()[0];
        if (!pt.getRawType().equals(MxGlobalEventContext.class)) {
            throw new IllegalArgumentException(
                    "@MxSubscribe method " + methodRef
                            + " parameter must be MxGlobalEventContext<T, W>, found: " + pt.getRawType());
        }
        Type[] typeArgs = pt.getActualTypeArguments();
        Class<?> eventClass = (Class<?>) typeArgs[0];
        if (!MxGlobalEvent.class.isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException(
                    "@MxSubscribe method " + methodRef
                            + " MxGlobalEventContext type argument " + eventClass.getName()
                            + " does not extend MxGlobalEvent");
        }
        Class<MxGlobalEvent<W>> typedEventClass = (Class<MxGlobalEvent<W>>) eventClass;

        // Read @MxSubscribe metadata
        MxSubscribe sub = method.getAnnotation(MxSubscribe.class);
        MxPriority priority = sub.priority();
        String name = sub.name().isBlank() ? method.getName() : sub.name();
        boolean ignoreCancelled = sub.ignoreCancelled();

        // Resolve permitted world types via the injected guard strategy
        Set<W> allowedWorlds = worldTypeGuard.resolve(method);

        method.setAccessible(true);
        bus.register(
                typedEventClass,
                name,
                priority,
                allowedWorlds,
                ignoreCancelled,
                ctx -> {
                    try {
                        method.invoke(listener, ctx);
                    } catch (java.lang.reflect.InvocationTargetException ite) {
                        Throwable cause = ite.getCause();
                        if (cause instanceof RuntimeException re) throw re;
                        throw new RuntimeException(cause);
                    }
                },
                listener);
    }

    @Override
    protected void doUnbind(MxGlobalEventBus<W> bus, Object listener) {
        bus.unregisterListener(listener);
    }
}



