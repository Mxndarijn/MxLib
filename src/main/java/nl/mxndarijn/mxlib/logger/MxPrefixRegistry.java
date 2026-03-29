package nl.mxndarijn.mxlib.logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MxPrefixRegistry {
    private static final Map<String, MxPrefixType> PREFIXES = new ConcurrentHashMap<>();

    private MxPrefixRegistry() {}

    public static void register(MxPrefixType prefix) {
        PREFIXES.put(prefix.getName(), prefix);
    }

    public static <E extends Enum<E> & MxPrefixType> void registerAll(Class<E> enumClass) {
        for (E value : enumClass.getEnumConstants()) {
            register(value);
        }
    }

    public static MxPrefixType get(String name) {
        return PREFIXES.get(name);
    }

    public static Collection<MxPrefixType> all() {
        return Collections.unmodifiableCollection(PREFIXES.values());
    }
}
