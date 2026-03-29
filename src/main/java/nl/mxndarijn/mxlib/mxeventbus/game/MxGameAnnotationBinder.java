package nl.mxndarijn.mxlib.mxeventbus.game;

import nl.mxndarijn.mxlib.mxeventbus.core.MxAbstractAnnotationBinder;
import nl.mxndarijn.mxlib.mxeventbus.core.MxPriority;
import nl.mxndarijn.mxlib.mxeventbus.core.MxSubscribe;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Generic annotation binder that scans an object for {@link MxSubscribe}-annotated methods
 * and registers them into a {@link MxGameEventBus}.
 *
 * <p><b>Method signature requirements</b></p>
 * <p>Each {@code @MxSubscribe} method must declare exactly one parameter typed as
 * {@code MxEventContext<T,S,A>} where {@code T} is a concrete subclass of {@link MxGameEvent},
 * and must return {@code void}. Violations throw {@link IllegalArgumentException} at bind time.</p>
 *
 * <p><b>Guard resolution</b></p>
 * <p>Allowed game states and actor roles are resolved by the injected
 * {@link MxIGameStateGuard} and {@link MxIApplicableGuard} strategies respectively.
 * If a guard returns an empty set, the handler runs for all values.</p>
 *
 * @param <S> the concrete game-state type, which must implement {@link MxIGameState}
 * @param <A> the concrete actor-role type, which must implement {@link MxIApplicableTo}
 */
public class MxGameAnnotationBinder<S extends MxIGameState, A extends MxIApplicableTo>
        extends MxAbstractAnnotationBinder<MxGameEventBus<S, A>> {

    private final MxIGameStateGuard<S> stateGuard;
    private final MxIApplicableGuard<A> applicableGuard;

    /**
     * Constructs a new {@code MxGameAnnotationBinder} with the given guard strategies.
     *
     * @param stateGuard      resolves allowed game states from a handler method; must not be {@code null}
     * @param applicableGuard resolves allowed actor roles from a handler method; must not be {@code null}
     */
    public MxGameAnnotationBinder(MxIGameStateGuard<S> stateGuard, MxIApplicableGuard<A> applicableGuard) {
        if (stateGuard == null)      throw new IllegalArgumentException("stateGuard must not be null");
        if (applicableGuard == null) throw new IllegalArgumentException("applicableGuard must not be null");
        this.stateGuard      = stateGuard;
        this.applicableGuard = applicableGuard;
    }

    /**
     * Scans {@code listener} for {@link MxSubscribe}-annotated methods and registers them into {@code bus}.
     *
     * @param bus      the event bus to register handlers into; must not be {@code null}
     * @param listener the object whose methods are scanned; must not be {@code null}
     */
    public void bind(MxGameEventBus<S, A> bus, Object listener) {
        doBind(bus, listener);
    }

    /**
     * Removes all handlers registered by {@code listener} from {@code bus}.
     *
     * @param bus      the event bus to remove handlers from; must not be {@code null}
     * @param listener the object whose handlers should be removed; must not be {@code null}
     */
    public void unbind(MxGameEventBus<S, A> bus, Object listener) {
        doUnbindAll(bus, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerHandler(MxGameEventBus<S, A> bus, Object listener, Method method) {
        String methodRef = listener.getClass().getSimpleName() + "#" + method.getName();

        // Validate and infer MxEventContext<T,S,A> parameter
        ParameterizedType pt = (ParameterizedType) method.getGenericParameterTypes()[0];
        if (!pt.getRawType().equals(MxEventContext.class)) {
            throw new IllegalArgumentException(
                    "@MxSubscribe method " + methodRef
                            + " parameter must be MxEventContext<T,S,A>, found: " + pt.getRawType());
        }
        Type[] typeArgs = pt.getActualTypeArguments();
        Class<?> eventClass = (Class<?>) typeArgs[0];
        if (!MxGameEvent.class.isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException(
                    "@MxSubscribe method " + methodRef
                            + " MxEventContext type argument " + eventClass.getName()
                            + " does not extend MxGameEvent");
        }
        Class<MxGameEvent<S>> typedEventClass = (Class<MxGameEvent<S>>) eventClass;

        // Read @MxSubscribe metadata
        MxSubscribe sub = method.getAnnotation(MxSubscribe.class);
        MxPriority priority = sub.priority();
        String name = sub.name().isBlank() ? method.getName() : sub.name();
        boolean ignoreCancelled = sub.ignoreCancelled();

        // Read guard annotations via injected strategies
        Set<S> allowedStates  = stateGuard.resolve(method);
        Set<A> allowedTargets = applicableGuard.resolve(method);

        method.setAccessible(true);
        bus.register(
                typedEventClass,
                name,
                priority,
                allowedStates,
                allowedTargets,
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
    protected void doUnbind(MxGameEventBus<S, A> bus, Object listener) {
        bus.unregisterListener(listener);
    }
}


