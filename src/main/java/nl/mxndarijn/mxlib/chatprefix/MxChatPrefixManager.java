package nl.mxndarijn.mxlib.chatprefix;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for chat prefixes.
 * Supports default prefixes and allows modules to register additional ones dynamically.
 */
public final class MxChatPrefixManager {

    private static MxChatPrefixManager instance;

    private final Map<String, MxChatPrefixType> prefixes = new ConcurrentHashMap<>();

    private MxChatPrefixManager() {
        registerAll(MxStandardChatPrefix.class);
    }

    /** Singleton access. */
    public static MxChatPrefixManager getInstance() {
        if (instance == null) {
            instance = new MxChatPrefixManager();
        }
        return instance;
    }

    /** Register a single custom prefix. */
    public void register(MxChatPrefixType prefix) {
        if (prefix == null || prefix.getName() == null) return;
        prefixes.put(prefix.getName().toLowerCase(Locale.ROOT), prefix);
    }

    /** Register all enum constants of an enum implementing MxChatPrefixType. */
    public <E extends Enum<E> & MxChatPrefixType> void registerAll(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            register(e);
        }
    }

    /** Get a prefix by name (case-insensitive). */
    public Optional<MxChatPrefixType> find(MxChatPrefixType type) {
        if (type.getName() == null) return Optional.empty();
        return Optional.ofNullable(prefixes.get(type.getName().toLowerCase(Locale.ROOT)));
    }

    public MxChatPrefixType requireFind(MxChatPrefixType type) {
        return prefixes.get(type.getName().toLowerCase(Locale.ROOT));
    }

    /** Returns all registered prefixes. */
    public Collection<MxChatPrefixType> all() {
        return Collections.unmodifiableCollection(prefixes.values());
    }
}
