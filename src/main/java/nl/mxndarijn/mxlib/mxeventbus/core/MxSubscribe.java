package nl.mxndarijn.mxlib.mxeventbus.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler to be discovered and registered by
 * {@code AnnotationBinder}. The annotated method must have exactly one parameter
 * of type {@code MxEventContext<T>} where {@code T} extends {@code MxGameEvent}.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Subscribe(priority = MxPriority.HIGH)
 * @States({GameState.PLAYING})
 * @Applicable({ApplicableTo.PLAYER, ApplicableTo.HOST})
 * public void onCraft(MxEventContext<CraftAttemptEvent> ctx) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MxSubscribe {

    /**
     * The priority at which this handler runs relative to other handlers for the same event.
     * Lower enum ordinals run first.
     *
     * @return the handler priority; defaults to {@link MxPriority#NORMAL}
     */
    MxPriority priority() default MxPriority.NORMAL;

    /**
     * When {@code true}, this handler is skipped if the event has been cancelled.
     * Cancellation support is minimal in the current implementation and reserved for future use.
     *
     * @return {@code true} to skip cancelled events; defaults to {@code false}
     */
    boolean doNotRunWhenEventCanceled() default false;

    /**
     * Optional human-readable name for this handler, used in trace output and diagnostics.
     * Defaults to an empty string, in which case the method name is used.
     *
     * @return the handler name; empty string means use the method name
     */
    String name() default "";
}


