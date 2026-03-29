package nl.mxndarijn.mxlib.mxeventbus.core;

/**
 * Represents the cancellation verdict for an event travelling through the pipeline.
 *
 * <p>States are ordered from most-denied to most-allowed. The enum ordinal encodes
 * this ordering: a lower ordinal is "more denied", a higher ordinal is "more allowed".
 * Use {@link #isDenied()} and {@link #isAllowed()} for semantic checks.</p>
 *
 * <table border="1">
 *   <caption>State semantics</caption>
 *   <tr><th>State</th><th>Meaning</th><th>Can be overridden?</th></tr>
 *   <tr><td>HARD_DENY</td><td>Definitively cancelled; no handler may upgrade it.</td><td>No</td></tr>
 *   <tr><td>SOFT_DENY</td><td>Cancelled by default; a later handler may upgrade to SOFT_ALLOW or higher.</td><td>Yes</td></tr>
 *   <tr><td>PASS</td><td>Neutral / no opinion; the event proceeds unless something denies it.</td><td>Yes</td></tr>
 *   <tr><td>SOFT_ALLOW</td><td>Explicitly allowed; a later handler may still hard-deny it.</td><td>Yes</td></tr>
 *   <tr><td>HARD_ALLOW</td><td>Definitively allowed; no handler may downgrade it.</td><td>No</td></tr>
 * </table>
 *
 * <p>The {@code MxGameEventBus} enforces the "hard" states: once {@code HARD_DENY} or
 * {@code HARD_ALLOW} is set, subsequent calls to
 * {@code MxEventContext#setCancellationState(MxCancellationState)} are silently ignored.</p>
 */
public enum MxCancellationState {

    /**
     * Definitively cancelled. No subsequent handler may upgrade this verdict.
     * The event should be treated as fully blocked by the caller of
     * {@code MxGameEventBus#post(MxGameEvent)}.
     */
    HARD_DENY,

    /**
     * Soft cancellation. The event is currently denied but a later handler with
     * sufficient authority may upgrade the verdict to {@link #SOFT_ALLOW} or
     * {@link #HARD_ALLOW}.
     */
    SOFT_DENY,

    /**
     * Neutral verdict. The event is neither explicitly allowed nor denied.
     * This is the typical default for events that do not declare a
     * {@link MxDefaultCancellation} annotation.
     */
    PASS,

    /**
     * Soft allowance. The event is currently permitted but a later handler may
     * still downgrade it to {@link #SOFT_DENY} or {@link #HARD_DENY}.
     */
    SOFT_ALLOW,

    /**
     * Definitively allowed. No subsequent handler may downgrade this verdict.
     * The event should be treated as fully permitted by the caller of
     * {@code MxGameEventBus#post(MxGameEvent)}.
     */
    HARD_ALLOW;

    /**
     * Returns {@code true} if this state represents any form of denial
     * ({@link #HARD_DENY} or {@link #SOFT_DENY}).
     *
     * @return {@code true} when the event is currently denied
     */
    public boolean isDenied() {
        return this == HARD_DENY || this == SOFT_DENY;
    }

    /**
     * Returns {@code true} if this state represents any form of allowance
     * ({@link #SOFT_ALLOW} or {@link #HARD_ALLOW}).
     *
     * @return {@code true} when the event is currently explicitly allowed
     */
    public boolean isAllowed() {
        return this == SOFT_ALLOW || this == HARD_ALLOW;
    }

    /**
     * Returns {@code true} if this state is "hard" and therefore immutable once set.
     *
     * @return {@code true} for {@link #HARD_DENY} and {@link #HARD_ALLOW}
     */
    public boolean isHard() {
        return this == HARD_DENY || this == HARD_ALLOW;
    }

    @Override
    public String toString() {
        return switch (this) {
            case HARD_DENY -> "<dark_red>" + name() + "<yellow>";
            case SOFT_DENY -> "<red>" + name() + "<yellow>";
            case PASS -> "<gray>" + name() + "<yellow>";
            case SOFT_ALLOW -> "<green>" + name() + "<yellow>";
            case HARD_ALLOW -> "<dark_green>" + name() + "<yellow>";
        };
    }
}


