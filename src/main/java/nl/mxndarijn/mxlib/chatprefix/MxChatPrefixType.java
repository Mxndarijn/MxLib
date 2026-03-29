package nl.mxndarijn.mxlib.chatprefix;

/**
 * Contract for all chat prefixes. Implementations define a text prefix used in messages.
 * This interface allows registering custom prefixes from other modules.
 */
public interface MxChatPrefixType {
    /**
     * The raw prefix (already formatted, e.g., {@literal "<green>WIDM » <gray>"}).
     */
    String prefix();

    /** The display name or logical identifier (e.g., "WIDM"). */
    String getName();
}

