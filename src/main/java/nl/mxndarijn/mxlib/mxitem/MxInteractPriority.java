package nl.mxndarijn.mxlib.mxitem;

/**
 * Priority levels for {@link MxInteractPipeline} subscribers.
 * Subscribers with lower priority ordinal are called first.
 */
public enum MxInteractPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST
}
