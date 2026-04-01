package nl.mxndarijn.mxlib.spawnprotection.events.base;

/**
 * Central registry for spawn-pipeline strategy dependencies.
 *
 * <p>Must be initialised once at plugin startup (before any spawn events are fired) by
 * calling {@link #setWorldTypeResolver(MxIWorldTypeResolver)}. All spawn-pipeline event
 * constructors delegate world-type resolution to the registered {@link MxIWorldTypeResolver}.</p>
 *
 * <p>This indirection removes the only WIDM-specific reference from the event classes,
 * making them safe to move to MxLib.</p>
 */
public final class MxSpawnEventRegistry {

    private static MxIWorldTypeResolver worldTypeResolver;

    private MxSpawnEventRegistry() {}

    /**
     * Registers the {@link MxIWorldTypeResolver} to use for all spawn-pipeline events.
     * Must be called before any spawn events are constructed.
     *
     * @param resolver the resolver to register; must not be {@code null}
     * @throws IllegalArgumentException if {@code resolver} is {@code null}
     */
    public static void setWorldTypeResolver(MxIWorldTypeResolver resolver) {
        if (resolver == null) throw new IllegalArgumentException("resolver must not be null");
        worldTypeResolver = resolver;
    }

    /**
     * Returns the registered {@link MxIWorldTypeResolver}.
     *
     * @return the resolver; never {@code null}
     * @throws IllegalStateException if no resolver has been registered yet
     */
    public static MxIWorldTypeResolver getWorldTypeResolver() {
        if (worldTypeResolver == null)
            throw new IllegalStateException("MxSpawnEventRegistry has not been initialised. Call setWorldTypeResolver() at plugin startup.");
        return worldTypeResolver;
    }
}

