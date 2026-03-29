package nl.mxndarijn.mxlib.mxeventbus.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the default {@link MxCancellationState} and {@link MxVerdictResolver} strategy
 * for an event class.
 *
 * <p>Place this annotation on a {@code MxGameEvent} subclass to specify:</p>
 * <ul>
 *   <li>The initial cancellation verdict ({@link #value()}) that the {@code MxEventContext}
 *       starts with — used as the fallback if no handler submits a verdict.</li>
 *   <li>The {@link MxVerdictResolver} ({@link #resolver()}) that combines all handler-submitted
 *       verdicts into a single final state before MONITOR-priority handlers run.</li>
 * </ul>
 *
 * <p>If the annotation is absent, the default state is {@link MxCancellationState#PASS}
 * and the resolver is {@link MxVerdictResolver#HARD_DENY_WINS}.</p>
 *
 * <p>Example — crafting is soft-denied by default, and any hard-deny beats everything:</p>
 * <pre>{@code
 * @DefaultCancellation(value = MxCancellationState.SOFT_DENY, resolver = MxVerdictResolver.HARD_DENY_WINS)
 * public class CraftAttemptEvent extends MxGameEvent implements MxHasActor { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MxDefaultCancellation {

    /**
     * The initial {@link MxCancellationState} used as the fallback verdict when no handler
     * submits a verdict during dispatch.
     *
     * @return the default cancellation state; defaults to {@link MxCancellationState#PASS}
     */
    MxCancellationState value() default MxCancellationState.PASS;

    /**
     * The {@link MxVerdictResolver} strategy used to combine all handler-submitted verdicts
     * into a single final {@link MxCancellationState} before MONITOR-priority handlers run.
     *
     * @return the resolver strategy; defaults to {@link MxVerdictResolver#HARD_DENY_WINS}
     */
    MxVerdictResolver resolver() default MxVerdictResolver.HARD_DENY_WINS;
}


