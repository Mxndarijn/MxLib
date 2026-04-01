package nl.mxndarijn.mxlib.spawnprotection.events.base;

import nl.mxndarijn.mxlib.mxeventbus.core.MxSubscribe;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventBus;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldTypes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Base class for all annotation-driven global event listeners.
 *
 * <p>Subclasses declare handler methods annotated with {@link MxSubscribe} and
 * {@link MxWorldTypes}, and are registered into the {@link MxGlobalEventBus} via
 * {@link WidmGlobalAnnotationBinder#bind(MxGlobalEventBus, Object)}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class MySpawnListener extends MxGlobalEventListener {
 *     @MxSubscribe
 *     @MxWorldTypes(MxWorldType.SPAWN)
 *     public void onChat(MxGlobalEventContext<MxGlobalChatEvent, MxWorldType> ctx) { ... }
 * }
 * }</pre>
 */
public abstract class MxGlobalEventListener {

    /**
     * Looks up an online {@link Player} by UUID, throwing if not found.
     *
     * @param uuid the UUID to look up; must not be {@code null}
     * @return the online player; never {@code null}
     * @throws IllegalStateException if no online player with the given UUID exists
     */
    protected Player requirePlayer(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) throw new IllegalStateException("Online player not found for UUID: " + uuid);
        return p;
    }
}

