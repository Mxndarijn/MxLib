package nl.mxndarijn.mxlib.mxeventbus.global;

import nl.mxndarijn.mxlib.mxeventbus.core.MxBaseContext;
import nl.mxndarijn.mxlib.mxeventbus.core.MxCancellationState;
import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps a {@link MxGlobalEvent} as it travels through the {@link MxGlobalEventBus} pipeline.
 *
 * <p>Handlers receive a {@code MxGlobalEventContext<T, W>} rather than the raw event,
 * giving them access to both the event and pipeline metadata such as the execution
 * trace and the resolved cancellation state.</p>
 *
 * <p><b>Cancellation</b></p>
 * <p>Handlers may call {@link #setCancelled(boolean)} for simple boolean cancellation, or
 * {@link #submitVerdict(MxCancellationState)} to participate in the verdict-resolution system
 * (matching the game pipeline). Before MONITOR-priority handlers run, the bus calls
 * {@link #resolveVerdict()} which converts all submitted verdicts into the final
 * {@link MxCancellationState}. MONITOR-priority handlers see the resolved state.</p>
 *
 * @param <T> the concrete event type, which must extend {@link MxGlobalEvent}
 * @param <W> the world-type enum used by the event
 */
public final class MxGlobalEventContext<T extends MxGlobalEvent<W>, W extends MxIWorldType> implements MxBaseContext<T> {

    private final T event;
    private final List<String> trace = new ArrayList<>();
    private final List<MxCancellationState> verdicts = new ArrayList<>();
    private MxCancellationState resolvedState = MxCancellationState.PASS;

    /**
     * Creates a new context wrapping the given event.
     *
     * @param event the event being dispatched; must not be {@code null}
     */
    public MxGlobalEventContext(T event) {
        if (event == null) throw new IllegalArgumentException("event must not be null");
        this.event = event;
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
     * Returns whether the event has been cancelled by a handler.
     *
     * @return {@code true} if the current {@link MxCancellationState} is denied
     */
    public boolean isCancelled() {
        return resolvedState.isDenied();
    }

    /**
     * Sets the cancellation state using a simple boolean.
     * {@code true} submits {@link MxCancellationState#HARD_DENY};
     * {@code false} submits {@link MxCancellationState#PASS}.
     *
     * @param cancelled {@code true} to cancel the event, {@code false} to uncancel it
     */
    public void setCancelled(boolean cancelled) {
        resolvedState = cancelled ? MxCancellationState.HARD_DENY : MxCancellationState.PASS;
    }

    /**
     * Submits a cancellation verdict from a handler.
     * All submitted verdicts are collected and resolved by the bus before MONITOR handlers run.
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
     * Resolves all submitted verdicts into the final {@link MxCancellationState}.
     * Called by the bus before MONITOR-priority handlers run.
     * Uses {@code HARD_DENY_WINS} semantics: any HARD_DENY wins; otherwise the
     * most-denied non-hard verdict wins; if no verdicts, the current state is kept.
     */
    public void resolveVerdict() {
        if (verdicts.isEmpty()) return;
        MxCancellationState result = MxCancellationState.PASS;
        for (MxCancellationState v : verdicts) {
            if (v == MxCancellationState.HARD_DENY) { result = v; break; }
            if (v.ordinal() < result.ordinal()) result = v;
        }
        resolvedState = result;
        trace("  <yellow>[<gold>RESOLVE<yellow>] " + verdicts + " \u2192 " + resolvedState);
    }

    /**
     * Returns the current {@link MxCancellationState}.
     *
     * @return the current state; never {@code null}
     */
    public MxCancellationState getCancellationState() {
        return resolvedState;
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






