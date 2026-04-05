package nl.mxndarijn.mxlib.chatprefix;

/**
 * Contract for all chat prefixes. Implementations define a text prefix used in messages.
 * This interface allows registering custom prefixes from other modules.
 */
public interface MxChatPrefixType {
    /**
     * Returns the raw prefix (already formatted, e.g., {@literal "<green>WIDM » <gray>"}).
     * @return the prefix string
     */
    String prefix();

    /**
     * Returns the display name or logical identifier (e.g., "WIDM").
     * @return the name
     */
    String getName();
}

