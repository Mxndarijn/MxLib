package nl.mxndarijn.mxlib.mxeventbus.global;

import nl.mxndarijn.mxlib.configfiles.MxConfigFileType;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.mxeventbus.core.MxAbstractEventBus;
import nl.mxndarijn.mxlib.mxeventbus.core.MxBaseContext;
import nl.mxndarijn.mxlib.mxeventbus.core.MxBaseHandlerEntry;
import nl.mxndarijn.mxlib.mxeventbus.core.MxPriority;
import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldType;

import java.util.Set;

/**
 * Plugin-wide event bus for events that occur outside of a game context.
 *
 * <p>Extends {@link MxAbstractEventBus} with a world-type guard: handlers may be
 * annotated with {@link MxWorldTypes} to restrict execution to specific world categories.
 * When the annotation is absent the handler runs for all world types.</p>
 *
 * <p>Events originating from a {@link MxWorldType#GAME} world are skipped — they are
 * handled exclusively by the {@code MxGameEventBus}.</p>
 *
 * <p>Prefer registering handlers via {@link MxGlobalAnnotationBinder} rather than calling
 * {@link #register} directly.</p>
 *
 * @param <W> the world-type enum that categorises worlds for this pipeline
 */
public class MxGlobalEventBus<W extends MxIWorldType> extends MxAbstractEventBus<MxGlobalEvent<W>, MxBaseContext<MxGlobalEvent<W>>, MxBaseHandlerEntry<MxGlobalEvent<W>, ?>> {

    /** The world-type value that represents a game world; events from this type are skipped. */
    private final W gameWorldType;

    /** The config file used to persist logged event names. */
    private final MxConfigFileType configFile;

    /**
     * Constructs a new {@code MxGlobalEventBus}.
     *
     * @param gameWorldType the world-type value that represents a game world; must not be {@code null}
     * @param configFile    the config file used to persist logged event names; must not be {@code null}
     */
    public MxGlobalEventBus(W gameWorldType, MxConfigFileType configFile) {
        if (gameWorldType == null) throw new IllegalArgumentException("gameWorldType must not be null");
        if (configFile == null)    throw new IllegalArgumentException("configFile must not be null");
        this.gameWorldType = gameWorldType;
        this.configFile = configFile;
    }


    /**
     * Registers a handler for the given event type.
     *
     * @param <T>             the event type
     * @param eventType       the exact event class to listen for; must not be {@code null}
     * @param name            human-readable handler name used in trace output
     * @param priority        execution priority; must not be {@code null}
     * @param allowedWorlds   set of permitted world-type values, or empty for all
     * @param ignoreCancelled when {@code true}, skip this handler if the event is cancelled
     * @param handler         the handler logic; must not be {@code null}
     * @param listener        the owning listener object; may be {@code null}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends MxGlobalEvent<W>> void register(Class<T> eventType,
                                                     String name,
                                                     MxPriority priority,
                                                     Set<W> allowedWorlds,
                                                     boolean ignoreCancelled,
                                                     MxGlobalEventHandler<T, W> handler,
                                                     Object listener) {
        if (eventType == null) throw new IllegalArgumentException("eventType must not be null");
        if (priority == null)  throw new IllegalArgumentException("priority must not be null");
        if (handler == null)   throw new IllegalArgumentException("handler must not be null");

        MxGlobalHandlerEntry entry = new MxGlobalHandlerEntry<>(
                name != null ? name : "anonymous",
                priority, allowedWorlds, ignoreCancelled, handler, listener);
        registerEntry(eventType, entry);
    }


    /**
     * Dispatches the given event through the pipeline.
     * Events originating from the game world type are skipped.
     *
     * @param event the event to dispatch; must not be {@code null}
     * @return the context that was passed through the pipeline,
     *         or {@code null} if the event originates from a game world
     */
    @Override
    public MxBaseContext<MxGlobalEvent<W>> post(MxGlobalEvent<W> event) {
        if (event == null) throw new IllegalArgumentException("event must not be null");
        if (event.worldType() == gameWorldType) {
            return null;
        }
        return super.post(event);
    }


    @Override
    @SuppressWarnings("unchecked")
    protected MxBaseContext<MxGlobalEvent<W>> createContext(MxGlobalEvent<W> event) {
        return (MxBaseContext<MxGlobalEvent<W>>) new MxGlobalEventContext<>(event);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean passesGuards(MxBaseHandlerEntry<MxGlobalEvent<W>, ?> entry, MxBaseContext<MxGlobalEvent<W>> ctx, MxGlobalEvent<W> event) {
        if (entry.ignoreCancelled && ctx.isCancelled()) {
            ctx.trace("<yellow>[<gray>SKIP<yellow>] " + entry.name
                    + " — ignoreCancelled=true and event is cancelled");
            return false;
        }

        MxGlobalHandlerEntry<?, W> globalEntry = (MxGlobalHandlerEntry<?, W>) entry;
        if (globalEntry.allowedWorlds != null && !globalEntry.allowedWorlds.isEmpty()
                && !globalEntry.allowedWorlds.contains(event.worldType())) {
            ctx.trace("<yellow>[<gray>SKIP<yellow>] " + entry.name
                    + " — worldType " + event.worldType()
                    + " not in " + globalEntry.allowedWorlds);
            return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void invokeHandler(MxBaseHandlerEntry<MxGlobalEvent<W>, ?> entry, MxBaseContext<MxGlobalEvent<W>> ctx) {
        try {
            int verdictsBefore = ctx.getSubmittedVerdicts().size();
            ctx.trace("<yellow>[<green>RUN<yellow>]  " + entry.name
                    + " (priority=" + entry.priority + ")");
            ((MxGlobalEventHandler) entry.handler).handle((MxGlobalEventContext) ctx);
            var verdicts = ctx.getSubmittedVerdicts();
            if (verdicts.size() > verdictsBefore) {
                ctx.trace("<yellow>  <dark_gray>\u21b3 <yellow>Verdict: " + verdicts.get(verdicts.size() - 1));
            }
        } catch (Exception e) {
            MxLogger.logMessage(MxLogLevel.ERROR,
                    "[MxGlobalEventBus] Handler '" + entry.name
                            + "' threw an exception: " + e.getMessage());
        }
    }

    @Override
    protected MxConfigFileType loggedEventsConfigFile() {
        return configFile;
    }

    @Override
    protected String loggedEventsConfigKey() {
        return "global-logged-events";
    }

    @Override
    protected String busLabel() {
        return "MxGlobalEventBus";
    }
}






