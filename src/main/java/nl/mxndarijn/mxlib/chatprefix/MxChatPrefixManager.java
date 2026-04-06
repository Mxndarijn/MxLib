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

    /**
     * Singleton access.
     * @return the singleton instance
     */
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

    /**
     * Registers all enum constants of an enum implementing {@link MxChatPrefixType}.
     *
     * @param <E>       the enum type
     * @param enumClass the enum class whose constants should be registered
     */
    public <E extends Enum<E> & MxChatPrefixType> void registerAll(Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            register(e);
        }
    }

    /**
     * Finds a prefix by type.
     * @param type the prefix type to find
     * @return an {@link Optional} containing the prefix if found
     */
    public Optional<MxChatPrefixType> find(MxChatPrefixType type) {
        if (type.getName() == null) return Optional.empty();
        return Optional.ofNullable(prefixes.get(type.getName().toLowerCase(Locale.ROOT)));
    }

    /**
     * Finds a prefix by type, throwing an exception if not found.
     * @param type the prefix type to find
     * @return the prefix
     */
    public MxChatPrefixType requireFind(MxChatPrefixType type) {
        return prefixes.get(type.getName().toLowerCase(Locale.ROOT));
    }

    /** Returns all registered prefixes. */
    public Collection<MxChatPrefixType> all() {
        return Collections.unmodifiableCollection(prefixes.values());
    }
}
