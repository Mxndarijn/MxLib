package nl.mxndarijn.mxlib.spawnprotection.events.base;

import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import org.bukkit.World;

/**
 * Strategy interface for resolving a Bukkit {@link World} to its {@link MxWorldType} category.
 *
 * <p>Implementations are registered via {@link MxSpawnEventRegistry} and used by all
 * spawn-pipeline event constructors to determine the world type at event creation time.</p>
 */
@FunctionalInterface
public interface MxIWorldTypeResolver {

    /**
     * Resolves the given world to its {@link MxWorldType}.
     *
     * @param world the world to resolve; must not be {@code null}
     * @return the resolved {@link MxWorldType}; never {@code null}
     */
    MxWorldType resolve(World world);
}

