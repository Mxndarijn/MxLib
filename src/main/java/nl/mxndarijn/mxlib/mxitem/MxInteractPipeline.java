package nl.mxndarijn.mxlib.mxitem;

import nl.mxndarijn.mxlib.MxLib;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * A separate pipeline for {@link PlayerInteractEvent} that fires independently of
 * Bukkit's cancellation state. This is needed because right-clicking air is
 * auto-cancelled by the server, making {@code ignoreCancelled = true} unreliable.
 *
 * <p>Subscribers register a {@link Consumer} of {@link MxPlayerInteractEvent} with a
 * {@link MxInteractPriority}. Subscribers are called in priority order (LOWEST first,
 * HIGHEST last). Each subscriber may call {@link MxPlayerInteractEvent#setCancelled(boolean)}
 * to signal cancellation to subsequent subscribers or the originating {@link MxItem}.
 */
public class MxInteractPipeline implements Listener {

    private static MxInteractPipeline instance;

    private final List<PriorityEntry> subscribers = new ArrayList<>();

    private MxInteractPipeline() {
        MxLib.getPlugin().getServer().getPluginManager().registerEvents(this, MxLib.getPlugin());
    }

    public static MxInteractPipeline getInstance() {
        if (instance == null) {
            instance = new MxInteractPipeline();
        }
        return instance;
    }

    /**
     * Subscribe to the interact pipeline with {@link MxInteractPriority#NORMAL} priority.
     *
     * @param subscriber the handler to register
     */
    public void subscribe(Consumer<MxPlayerInteractEvent> subscriber) {
        subscribe(subscriber, MxInteractPriority.NORMAL);
    }

    /**
     * Subscribe to the interact pipeline with the given priority.
     * Subscribers with lower priority ordinal (e.g. LOWEST) are called before higher ones.
     *
     * @param subscriber the handler to register
     * @param priority   the priority at which this subscriber should run
     */
    public void subscribe(Consumer<MxPlayerInteractEvent> subscriber, MxInteractPriority priority) {
        subscribers.add(new PriorityEntry(subscriber, priority));
        subscribers.sort(Comparator.comparingInt(e -> e.priority.ordinal()));
    }

    /**
     * Unsubscribe a previously registered consumer.
     *
     * @param subscriber the handler to remove
     */
    public void unsubscribe(Consumer<MxPlayerInteractEvent> subscriber) {
        subscribers.removeIf(e -> e.subscriber == subscriber);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent e) {
        MxPlayerInteractEvent mxEvent = new MxPlayerInteractEvent(e, e.getPlayer());
        for (PriorityEntry entry : new ArrayList<>(subscribers)) {
            entry.subscriber.accept(mxEvent);
            if (mxEvent.isCancelled()) {
                break;
            }
        }
    }

    private static class PriorityEntry {
        final Consumer<MxPlayerInteractEvent> subscriber;
        final MxInteractPriority priority;

        PriorityEntry(Consumer<MxPlayerInteractEvent> subscriber, MxInteractPriority priority) {
            this.subscriber = subscriber;
            this.priority = priority;
        }
    }
}
