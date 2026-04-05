package nl.mxndarijn.mxlib.mxeventbus.core;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Shared scanning and validation logic for annotation-driven event-bus binders.
 *
 * <p>Subclasses implement {@link #registerHandler} to perform the bus-specific
 * guard resolution and handler registration after the common signature checks
 * performed here.</p>
 *
 * @param <B> the concrete bus type this binder targets
 */
public abstract class MxAbstractAnnotationBinder<B> {

    /**
     * Default constructor for subclasses.
     */
    protected MxAbstractAnnotationBinder() {
    }

    /**
     * Scans all declared methods of {@code listener} for {@link MxSubscribe} annotations,
     * validates their signatures, and delegates each valid method to
     * {@link #registerHandler(Object, Object, Method)} for bus-specific registration.
     *
     * @param bus      the event bus to register handlers into; must not be {@code null}
     * @param listener the object whose methods are scanned; must not be {@code null}
     * @throws IllegalArgumentException if any {@code @MxSubscribe} method has an invalid signature
     */
    public final void doBind(B bus, Object listener) {
        if (bus == null) throw new IllegalArgumentException("bus must not be null");
        if (listener == null) throw new IllegalArgumentException("listener must not be null");
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(MxSubscribe.class)) continue;
            validateSignature(method, listener.getClass().getSimpleName());
            registerHandler(bus, listener, method);
        }
    }

    /**
     * Removes all handlers registered by the given listener from the bus.
     *
     * @param bus      the event bus to remove handlers from; must not be {@code null}
     * @param listener the object whose handlers should be removed; must not be {@code null}
     */
    public final void doUnbindAll(B bus, Object listener) {
        if (bus == null) throw new IllegalArgumentException("bus must not be null");
        if (listener == null) throw new IllegalArgumentException("listener must not be null");
        doUnbind(bus, listener);
    }

    /**
     * Validates that a {@link MxSubscribe}-annotated method has the correct signature:
     * returns {@code void}, has exactly one parameter, and that parameter is a
     * parameterised context type. Subclasses are responsible for further validation
     * of the type arguments in {@link #registerHandler}.
     *
     * @param method          the method to validate
     * @param ownerSimpleName simple name of the declaring class, used in error messages
     * @throws IllegalArgumentException if the signature is invalid
     */
    private void validateSignature(Method method, String ownerSimpleName) {
        String methodRef = ownerSimpleName + "#" + method.getName();

        if (!method.getReturnType().equals(void.class)) {
            throw new IllegalArgumentException(
                    "@MxSubscribe method " + methodRef + " must return void");
        }
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException(
                    "@MxSubscribe method " + methodRef
                            + " must have exactly one parameter, but found "
                            + method.getParameterCount() + " parameter(s)");
        }
        Type paramType = method.getGenericParameterTypes()[0];
        if (!(paramType instanceof ParameterizedType pt)) {
            throw new IllegalArgumentException(
                    "@MxSubscribe method " + methodRef
                            + " parameter must be a parameterised context type, not a raw type");
        }
    }

    /**
     * Performs bus-specific context-type validation, guard resolution, and handler registration.
     * Called once per valid {@link MxSubscribe}-annotated method after common signature validation.
     *
     * @param bus      the event bus to register into
     * @param listener the listener object owning the method
     * @param method   the validated handler method (not yet made accessible)
     */
    protected abstract void registerHandler(B bus, Object listener, Method method);

    /**
     * Removes all handlers belonging to {@code listener} from the bus.
     *
     * @param bus      the event bus to remove from
     * @param listener the listener whose handlers should be removed
     */
    protected abstract void doUnbind(B bus, Object listener);
}


