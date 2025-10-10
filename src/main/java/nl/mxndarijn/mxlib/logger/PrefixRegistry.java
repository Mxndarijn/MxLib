package nl.mxndarijn.mxlib.logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PrefixRegistry {
    private static final Map<String, PrefixType> PREFIXES = new ConcurrentHashMap<>();

    private PrefixRegistry() {}

    public static void register(PrefixType prefix) {
        PREFIXES.put(prefix.getName(), prefix);
    }

    public static <E extends Enum<E> & PrefixType> void registerAll(Class<E> enumClass) {
        for (E value : enumClass.getEnumConstants()) {
            register(value);
        }
    }

    public static PrefixType get(String name) {
        return PREFIXES.get(name);
    }

    public static Collection<PrefixType> all() {
        return Collections.unmodifiableCollection(PREFIXES.values());
    }
}
