package nl.mxndarijn.mxlib.mxeventbus.game;
import nl.mxndarijn.mxlib.mxeventbus.core.MxBaseContext;
import nl.mxndarijn.mxlib.mxeventbus.core.MxCancellationState;
import nl.mxndarijn.mxlib.mxeventbus.core.MxDefaultCancellation;
import nl.mxndarijn.mxlib.mxeventbus.core.MxVerdictResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps a {@link MxGameEvent} as it travels through the event pipeline.
 * Handlers receive an {@code MxEventContext<T,S,A>} rather than the raw event,
 * giving them access to both the event and pipeline metadata such as the
 * execution trace, submitted verdicts, and the resolved {@link MxCancellationState}.
 *
 * <p><b>Verdict collection</b></p>
 * <p>Each handler may call {@link #submitVerdict(MxCancellationState)} to cast a vote.
 * All submitted verdicts are collected in order. Before MONITOR-priority handlers run,
 * the bus calls {@link #resolveVerdict()} which feeds the full list to the
 * event's {@link MxVerdictResolver} and stores the result as the final {@link MxCancellationState}.</p>
 *
 * @param <T> the concrete event type, which must extend {@link MxGameEvent}
 * @param <S> the concrete game-state type, which must implement {@link MxIGameState}
 * @param <A> the concrete actor-role type, which must implement {@link MxIApplicableTo}
 */
public class MxEventContext<T extends MxGameEvent<S>, S extends MxIGameState, A extends MxIApplicableTo>
        implements MxBaseContext<T> {

    private final T event;
    private final List<String> trace = new ArrayList<>();
    private final List<MxCancellationState> verdicts = new ArrayList<>();
    private final MxCancellationState defaultState;
    private final MxVerdictResolver resolver;
    private MxCancellationState resolvedState;

    /**
     * Creates a new context wrapping the given event.
     * The default state and resolver are read from the {@link MxDefaultCancellation}
     * annotation on the event class, falling back to {@link MxCancellationState#PASS}
     * and {@link MxVerdictResolver#HARD_DENY_WINS} respectively.
     *
     * @param event the event being dispatched; must not be {@code null}
     */
    public MxEventContext(T event) {
        if (event == null) throw new IllegalArgumentException("event must not be null");
        this.event = event;
        MxDefaultCancellation ann = event.getClass().getAnnotation(MxDefaultCancellation.class);
        this.defaultState  = ann != null ? ann.value() : MxCancellationState.PASS;
        this.resolver      = ann != null ? ann.resolver() : MxVerdictResolver.HARD_DENY_WINS;
        this.resolvedState = this.defaultState;
    }

    /**
     * Returns the event being dispatched.
     *
     * @return the event; never {@code null}
     */
    public T event() {
        return event;
    }

    /**
     * Submits a cancellation verdict from a handler.
     *
     * @param state the verdict to submit; must not be {@code null}
     */
    public void submitVerdict(MxCancellationState state) {
        if (state == null) throw new IllegalArgumentException("state must not be null");
        verdicts.add(state);
    }

    /**
     * Returns an unmodifiable view of all verdicts submitted by handlers so far.
     *
     * @return ordered list of submitted verdicts; never {@code null}
     */
    public List<MxCancellationState> getSubmittedVerdicts() {
        return Collections.unmodifiableList(verdicts);
    }

    /**
     * Resolves all submitted verdicts into a single final {@link MxCancellationState}
     * using the event's {@link MxVerdictResolver}. Called by the bus before MONITOR-priority handlers run.
     */
    public void resolveVerdict() {
        resolvedState = resolver.resolve(verdicts, defaultState);
        trace("  [RESOLVE] " + verdicts + " → " + resolvedState
                + " (resolver=" + resolver.name() + ", default=" + defaultState + ")");
    }

    /**
     * Returns the current cancellation state.
     *
     * @return the current {@link MxCancellationState}; never {@code null}
     */
    public MxCancellationState getCancellationState() {
        return resolvedState;
    }

    /**
     * Returns a live preview of what the cancellation state would be if resolved right now.
     *
     * @return the would-be resolved {@link MxCancellationState}; never {@code null}
     */
    public MxCancellationState peekCurrentState() {
        return resolver.resolve(verdicts, defaultState);
    }

    /**
     * Returns {@code true} if the current cancellation state is any form of denial.
     *
     * @return {@code true} when the event is currently denied
     */
    public boolean isCancelled() {
        return resolvedState.isDenied();
    }

    /**
     * Appends a trace message describing a pipeline step.
     *
     * @param message the message to record; must not be {@code null}
     */
    public void trace(String message) {
        if (message == null) throw new IllegalArgumentException("message must not be null");
        trace.add(message);
    }

    /**
     * Returns an unmodifiable view of all trace messages recorded during dispatch.
     *
     * @return ordered list of trace messages; never {@code null}
     */
    public List<String> getTrace() {
        return Collections.unmodifiableList(trace);
    }
}


