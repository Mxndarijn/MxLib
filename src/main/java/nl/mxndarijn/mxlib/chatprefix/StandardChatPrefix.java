package nl.mxndarijn.mxlib.chatprefix;

import lombok.Getter;
import nl.mxndarijn.mxlib.chatprefix.ChatPrefixType;

/**
 * Default chat prefixes for WIDM.
 * Extendable through {@link ChatPrefixManager}.
 */
@Getter
public enum StandardChatPrefix implements ChatPrefixType {
    DEFAULT("<green>Default"),
    NO_PERMISSION("<red>Geen-Permissie");

    private final String prefix;
    private final String name;

    StandardChatPrefix(String name) {
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
