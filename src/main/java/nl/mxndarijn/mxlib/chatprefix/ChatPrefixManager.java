package nl.mxndarijn.mxlib.chatprefix;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for chat prefixes.
 * Supports default prefixes and allows modules to register additional ones dynamically.
 */
public final class ChatPrefixManager {

    private static ChatPrefixManager instance;

    private final Map<String, ChatPrefixType> prefixes = new ConcurrentHashMap<>();

    private ChatPrefixManager() {
        registerAll(StandardChatPrefix.class);
    }

    /** Singleton access. */
    public static ChatPrefixManager getInstance() {
        if (instance == null) {
            instance = new ChatPrefixManager();
        }
        return instance;
    }

    /** Register a single custom prefix. */
    public void register(ChatPrefixType prefix) {
        if (prefix == null || prefix.getName() == null) return;
        prefixes.put(prefix.getName().toLowerCase(Locale.ROOT), prefix);
    }

    /** Register all enum constants of an enum implementing ChatPrefixType. */
    public <E extends Enum<E> & ChatPrefixType> void registerAll(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            register(e);
        }
    }

    /** Get a prefix by name (case-insensitive). */
    public Optional<ChatPrefixType> find(ChatPrefixType type) {
        if (type.getName() == null) return Optional.empty();
        return Optional.ofNullable(prefixes.get(type.getName().toLowerCase(Locale.ROOT)));
    }

    public ChatPrefixType requireFind(ChatPrefixType type) {
        return prefixes.get(type.getName().toLowerCase(Locale.ROOT));
    }

    /** Returns all registered prefixes. */
    public Collection<ChatPrefixType> all() {
        return Collections.unmodifiableCollection(prefixes.values());
    }
}
