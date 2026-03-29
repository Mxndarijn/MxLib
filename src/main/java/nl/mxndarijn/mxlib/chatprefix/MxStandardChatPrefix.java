package nl.mxndarijn.mxlib.chatprefix;

import lombok.Getter;

/**
 * Default chat prefixes for WIDM.
 * Extendable through {@link MxChatPrefixManager}.
 */
@Getter
public enum MxStandardChatPrefix implements MxChatPrefixType {
    DEFAULT("<green>Default"),
    NO_PERMISSION("<red>Geen-Permissie");

    private final String prefix;
    private final String name;

    MxStandardChatPrefix(String name) {
        this.prefix = name + "<dark_green> » <gray>";
        this.name = name;
    }

    @Override
    public String prefix() {
        return prefix;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
