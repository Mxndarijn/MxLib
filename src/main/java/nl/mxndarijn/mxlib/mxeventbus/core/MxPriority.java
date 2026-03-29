package nl.mxndarijn.mxlib.mxeventbus.core;

/**
 * Defines the execution order of event handlers registered on the {@code MxGameEventBus}.
 * Handlers are invoked from {@link #LOWEST} up to {@link #HIGHEST}, then
 * {@link #MONITOR} runs after verdict resolution. Within the same priority level,
 * registration order is preserved.
 *
 * <p>Execution order: LOWEST → LOW → NORMAL → HIGH → HIGHEST → (resolve) → MONITOR</p>
 *
 * <p>{@link #MONITOR} is intended for read-only observation after all other handlers
 * have run and the final verdict has been resolved. MONITOR handlers should not
 * submit further verdicts.</p>
 */
public enum MxPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    MONITOR
}


