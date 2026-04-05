package nl.mxndarijn.mxlib.mxeventbus.core;

import nl.mxndarijn.mxlib.configfiles.MxConfigFileType;
import nl.mxndarijn.mxlib.configfiles.MxConfigService;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;

import java.util.*;

/**
 * Generic abstract base for annotation-driven event buses.
 *
 * <p>Provides the shared registry, logging infrastructure, and the three-phase
 * dispatch pipeline (non-MONITOR → resolution → MONITOR) used by both
 * the game pipeline and the global pipeline.</p>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #createContext(E)} — construct the appropriate context for an event.</li>
 *   <li>{@link #passesGuards(MxBaseHandlerEntry, MxBaseContext, MxBaseEvent)} — domain-specific
 *       guard evaluation (cancellation + extra guards).</li>
 *   <li>{@link #invokeHandler(MxBaseHandlerEntry, MxBaseContext)} — invoke the handler with
 *       the correct typed context.</li>
 *   <li>{@link #loggedEventsConfigKey()} — the config key used to persist logged event names.</li>
 *   <li>{@link #busLabel()} — the label used in trace log output.</li>
 * </ul>
 *
 * @param <E> the base event type for this bus
 * @param <C> the context type wrapping events
 * @param <H> the concrete handler-entry type
 */
public abstract class MxAbstractEventBus<E extends MxBaseEvent, C extends MxBaseContext<E>, H extends MxBaseHandlerEntry<E, ?>> {

    /**
     * Default constructor for subclasses.
     */
    protected MxAbstractEventBus() {
    }

    /** Maps event class → ordered list of handler entries. */
    private final Map<Class<? extends E>, List<H>> registry = new HashMap<>();

    /** Simple names of all event classes that have at least one registered handler. */
    private final Set<String> knownEvents = new LinkedHashSet<>();

    /** Simple names of event classes whose pipeline trace should be printed to console. */
    private final Set<String> loggedEvents = new HashSet<>();


    /**
     * Removes all registered handlers from the bus.
     */
    public void clear() {
        registry.clear();
        knownEvents.clear();
    }

    /**
     * Registers a handler entry for the given event type.
     * Entries are kept sorted by {@link MxPriority} ordinal; within the same priority
     * they appear in registration order.
     *
     * @param eventType the exact event class to listen for; must not be {@code null}
     * @param entry     the handler entry to register; must not be {@code null}
     */
    protected void registerEntry(Class<? extends E> eventType, H entry) {
        if (eventType == null) throw new IllegalArgumentException("eventType must not be null");
        if (entry == null)     throw new IllegalArgumentException("entry must not be null");

        registry.computeIfAbsent(eventType, k -> new ArrayList<>()).add(entry);
        registry.get(eventType).sort(Comparator.comparingInt(e -> e.priority.ordinal()));
        knownEvents.add(eventType.getSimpleName());
    }

    /**
     * Removes all handlers registered by the given listener object.
     *
     * @param listener the listener whose handlers should be removed; must not be {@code null}
     */
    public void unregisterListener(Object listener) {
        if (listener == null) return;
        registry.values().forEach(entries -> entries.removeIf(e -> e.listener == listener));
    }

    /**
     * Returns an unmodifiable view of all event simple names that have at least one
     * registered handler.
     *
     * @return set of simple class names; never {@code null}
     */
    public Set<String> getKnownEvents() {
        return Collections.unmodifiableSet(knownEvents);
    }


    /**
     * Loads the set of logged event names from the config file returned by
     * {@link #loggedEventsConfigFile()}.
     */
    public void loadLoggedEvents() {
        loggedEvents.clear();
        List<String> list = MxConfigService.getInstance()
            .get(loggedEventsConfigFile())
            .getCfg()
            .getStringList(loggedEventsConfigKey());
        loggedEvents.addAll(list);
    }

    /**
     * Saves the current set of logged event names to the config file returned by
     * {@link #loggedEventsConfigFile()}.
     */
    public void saveLoggedEvents() {
        MxConfigService.getInstance()
            .get(loggedEventsConfigFile())
            .getCfg()
            .set(loggedEventsConfigKey(), new ArrayList<>(loggedEvents));
        MxConfigService.getInstance()
            .get(loggedEventsConfigFile())
            .save();
    }

    /**
     * Adds an event class name to the logging set and persists the change.
     *
     * @param eventName simple class name of the event to log; must not be {@code null}
     * @return {@code true} if the name was newly added, {@code false} if already present
     */
    public boolean addLoggedEvent(String eventName) {
        boolean added = loggedEvents.add(eventName);
        if (added) saveLoggedEvents();
        return added;
    }

    /**
     * Removes an event class name from the logging set and persists the change.
     *
     * @param eventName simple class name of the event to stop logging; must not be {@code null}
     */
    public void removeLoggedEvent(String eventName) {
        boolean removed = loggedEvents.remove(eventName);
        if (removed) saveLoggedEvents();
    }

    /**
     * Clears all event names from the logging set and persists the change.
     */
    public void clearLoggedEvents() {
        if (!loggedEvents.isEmpty()) {
            loggedEvents.clear();
            saveLoggedEvents();
        }
    }

    /**
     * Returns an unmodifiable view of the currently logged event names.
     *
     * @return set of simple class names; never {@code null}
     */
    public Set<String> getLoggedEvents() {
        return Collections.unmodifiableSet(loggedEvents);
    }


    /**
     * Dispatches the given event through the three-phase pipeline.
     *
     * @param event the event to dispatch; must not be {@code null}
     * @return the context that was passed through the pipeline
     */
    @SuppressWarnings("unchecked")
    public C post(E event) {
        if (event == null) throw new IllegalArgumentException("event must not be null");

        C ctx = createContext(event);
        knownEvents.add(event.getClass().getSimpleName());

        List<H> entries = registry.getOrDefault(
                (Class<? extends E>) event.getClass(), Collections.emptyList());

        List<H> normal  = new ArrayList<>();
        List<H> monitor = new ArrayList<>();
        for (H e : entries) {
            if (e.priority == MxPriority.MONITOR) monitor.add(e);
            else normal.add(e);
        }

        // Phase 1: non-MONITOR handlers
        for (H entry : normal) {
            if (!passesGuards(entry, ctx, event)) continue;
            invokeHandler(entry, ctx);
        }

        // Resolution: combine all submitted verdicts into the final state
        ctx.resolveVerdict();

        // Phase 2: MONITOR handlers (see resolved state, read-only by convention)
        for (H entry : monitor) {
            if (!passesGuards(entry, ctx, event)) continue;
            invokeHandler(entry, ctx);
        }

        // Console logging
        if (loggedEvents.contains(event.getClass().getSimpleName())) {
            MxLogger.logMessage(MxLogLevel.INFORMATION,
                "[<aqua>" + busLabel() + "<yellow>] " + event.getClass().getSimpleName()
                    + " | verdict=" + ctx.getCancellationState());
            ctx.getTrace().forEach(line ->
                MxLogger.logMessage(MxLogLevel.INFORMATION, "  " + line));
        }

        return ctx;
    }


    /**
     * Creates a new context wrapping the given event.
     *
     * @param event the event being dispatched; never {@code null}
     * @return a fresh context; never {@code null}
     */
    protected abstract C createContext(E event);

    /**
     * Evaluates all guards for the given handler entry.
     * Must check at minimum the {@code ignoreCancelled} flag, then any domain-specific guards.
     *
     * @param entry the handler entry to evaluate; never {@code null}
     * @param ctx   the current event context; never {@code null}
     * @param event the event being dispatched; never {@code null}
     * @return {@code true} if the handler should run
     */
    protected abstract boolean passesGuards(H entry, C ctx, E event);

    /**
     * Invokes the handler with the correct typed context and appends a trace line.
     *
     * @param entry the handler to invoke; must not be {@code null}
     * @param ctx   the event context to pass to the handler; must not be {@code null}
     */
    protected abstract void invokeHandler(H entry, C ctx);

    /**
     * Returns the {@link MxConfigFileType} that stores the logged-event names for this bus.
     * Subclasses return the appropriate config-file descriptor for their deployment context.
     *
     * @return the config file descriptor; never {@code null}
     */
    protected abstract MxConfigFileType loggedEventsConfigFile();

    /**
     * Returns the YAML config key under which logged event names are persisted.
     *
     * @return the config key string; never {@code null}
     */
    protected abstract String loggedEventsConfigKey();

    /**
     * Returns the label used in trace log output (e.g. {@code "MxGameEventBus"} or {@code "MxGlobalEventBus"}).
     *
     * @return the bus label; never {@code null}
     */
    protected abstract String busLabel();
}



