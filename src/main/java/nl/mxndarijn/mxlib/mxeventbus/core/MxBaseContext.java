package nl.mxndarijn.mxlib.mxeventbus.core;

import java.util.List;

/**
 * Common interface for event pipeline contexts used by {@link MxAbstractEventBus}.
 *
 * <p>Concrete context implementations implement this interface so that the shared
 * pipeline logic in {@link MxAbstractEventBus} can operate on any context type without casting.</p>
 *
 * @param <E> the event type this context wraps
 */
public interface MxBaseContext<E extends MxBaseEvent> {

    /**
     * Returns the event being dispatched.
     *
     * @return the event; never {@code null}
     */
    E event();

    /**
     * Returns whether the current cancellation state is denied.
     *
     * @return {@code true} if the event is currently cancelled
     */
    boolean isCancelled();

    /**
     * Submits a cancellation verdict from a handler.
     *
     * @param state the verdict to submit; must not be {@code null}
     */
    void submitVerdict(MxCancellationState state);

    /**
     * Returns an unmodifiable view of all verdicts submitted so far.
     *
     * @return ordered list of submitted verdicts; never {@code null}
     */
    List<MxCancellationState> getSubmittedVerdicts();

    /**
     * Resolves all submitted verdicts into the final {@link MxCancellationState}.
     * Called by the bus before MONITOR-priority handlers run.
     */
    void resolveVerdict();

    /**
     * Returns the current (or resolved) {@link MxCancellationState}.
     *
     * @return the current state; never {@code null}
     */
    MxCancellationState getCancellationState();

    /**
     * Appends a trace message describing a pipeline step.
     *
     * @param message the message to record; must not be {@code null}
     */
    void trace(String message);

    /**
     * Returns an unmodifiable view of all trace messages recorded during dispatch.
     *
     * @return ordered list of trace messages; never {@code null}
     */
    List<String> getTrace();
}


