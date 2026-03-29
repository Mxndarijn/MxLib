package nl.mxndarijn.mxlib.mxeventbus.game;

import nl.mxndarijn.mxlib.configfiles.MxConfigFileType;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.mxeventbus.core.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Generic game-scoped event bus for the annotation-driven game event pipeline.
 *
 * <p>Extends {@link MxAbstractEventBus} with two additional guards per handler:
 * a game-state guard (resolved via {@link MxIGameStateGuard}) and an actor-role guard
 * (resolved via {@link MxIActorResolver}). One instance is created per game.</p>
 *
 * <p>WIDM-specific logic (actor resolution, config file) is injected via constructor
 * so this class has zero WIDM dependencies and can be moved to MxLib.</p>
 *
 * @param <S> the concrete game-state type, which must implement {@link MxIGameState}
 * @param <A> the concrete actor-role type, which must implement {@link MxIApplicableTo}
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MxGameEventBus<S extends MxIGameState, A extends MxIApplicableTo>
        extends MxAbstractEventBus<MxGameEvent<S>, MxBaseContext<MxGameEvent<S>>, MxBaseHandlerEntry<MxGameEvent<S>, ?>> {

    private final MxIActorResolver<A> actorResolver;
    private final MxConfigFileType configFile;

    /**
     * Constructs a new {@code MxGameEventBus} with the given actor resolver and config file.
     *
     * @param actorResolver the strategy used to resolve actor roles from UUIDs; must not be {@code null}
     * @param configFile    the config file used to persist logged-event settings; must not be {@code null}
     */
    public MxGameEventBus(MxIActorResolver<A> actorResolver, MxConfigFileType configFile) {
        if (actorResolver == null) throw new IllegalArgumentException("actorResolver must not be null");
        if (configFile == null)    throw new IllegalArgumentException("configFile must not be null");
        this.actorResolver = actorResolver;
        this.configFile    = configFile;
    }


    /**
     * Registers a handler for the given event type.
     *
     * @param <T>             the event type
     * @param eventType       the exact event class to listen for; must not be {@code null}
     * @param name            human-readable handler name used in trace output
     * @param priority        execution priority; must not be {@code null}
     * @param allowedStates   set of permitted game states, or {@code null}/empty for all
     * @param allowedTargets  set of permitted actor roles, or {@code null}/empty for all
     * @param ignoreCancelled when {@code true}, skip this handler if the event is denied
     * @param handler         the handler logic; must not be {@code null}
     */
    public <T extends MxGameEvent<S>> void register(Class<T> eventType,
                                                   String name,
                                                   MxPriority priority,
                                                   Set<S> allowedStates,
                                                   Set<A> allowedTargets,
                                                   boolean ignoreCancelled,
                                                   MxEventHandler<T> handler) {
        register(eventType, name, priority, allowedStates, allowedTargets, ignoreCancelled, handler, null);
    }

    /**
     * Registers a handler for the given event type with an owning listener reference.
     *
     * @param <T>             the event type
     * @param eventType       the exact event class to listen for; must not be {@code null}
     * @param name            human-readable handler name used in trace output
     * @param priority        execution priority; must not be {@code null}
     * @param allowedStates   set of permitted game states, or {@code null}/empty for all
     * @param allowedTargets  set of permitted actor roles, or {@code null}/empty for all
     * @param ignoreCancelled when {@code true}, skip this handler if the event is denied
     * @param handler         the handler logic; must not be {@code null}
     * @param listener        the owning listener object; may be {@code null}
     */
    public <T extends MxGameEvent<S>> void register(Class<T> eventType,
                                                   String name,
                                                   MxPriority priority,
                                                   Set<S> allowedStates,
                                                   Set<A> allowedTargets,
                                                   boolean ignoreCancelled,
                                                   MxEventHandler<T> handler,
                                                   Object listener) {
        if (eventType == null) throw new IllegalArgumentException("eventType must not be null");
        if (priority == null)  throw new IllegalArgumentException("priority must not be null");
        if (handler == null)   throw new IllegalArgumentException("handler must not be null");

        MxHandlerEntry entry = new MxHandlerEntry<>(
                name != null ? name : "anonymous",
                priority, allowedStates, allowedTargets, ignoreCancelled, handler, listener);
        registerEntry(eventType, entry);
    }

    /**
     * Unregisters all handlers belonging to the given listener object.
     *
     * @param listener the listener whose handlers should be removed; must not be {@code null}
     */
    public void unregister(Object listener) {
        if (listener == null) throw new IllegalArgumentException("listener must not be null");
        unregisterListener(listener);
    }


    @Override
    protected MxBaseContext<MxGameEvent<S>> createContext(MxGameEvent<S> event) {
        return (MxBaseContext<MxGameEvent<S>>) new MxEventContext<>(event);
    }

    @Override
    protected boolean passesGuards(MxBaseHandlerEntry<MxGameEvent<S>, ?> entry,
                                   MxBaseContext<MxGameEvent<S>> ctx,
                                   MxGameEvent<S> event) {
        if (entry.ignoreCancelled && ctx.isCancelled()) {
            ctx.trace("<yellow>[<gray>SKIP<yellow>] " + entry.name
                    + " — ignoreCancelled=true and state is " + ctx.getCancellationState() + "<yellow>");
            return false;
        }

        MxHandlerEntry<?> handlerEntry = (MxHandlerEntry<?>) entry;

        if (handlerEntry.allowedStates != null && !handlerEntry.allowedStates.isEmpty()) {
            if (!((Set<S>) handlerEntry.allowedStates).contains(event.gameState())) {
                String allowed = handlerEntry.allowedStates.stream()
                        .map(s -> "<yellow>" + s + "<yellow>")
                        .collect(Collectors.joining(", ", "[", "]"));
                ctx.trace("<yellow>[<gray>SKIP<yellow>] " + entry.name
                        + " — state <yellow>" + event.gameState()
                        + "<yellow> not in " + allowed + "<yellow>");
                return false;
            }
        }

        if (event instanceof MxHasActor hasActor) {
            UUID actorUUID = hasActor.getActor().orElse(null);
            Optional<A> applicableTo = actorResolver.resolve(actorUUID);
            if (applicableTo.isEmpty()) {
                ctx.trace("<yellow>[<gray>SKIP<yellow>] Actor not a known role<yellow>");
                return false;
            }
            if (handlerEntry.allowedTargets != null && !handlerEntry.allowedTargets.isEmpty()) {
                if (!((Set<A>) handlerEntry.allowedTargets).contains(applicableTo.get())) {
                    ctx.trace("<yellow>[<gray>SKIP<yellow>] " + entry.name + " — actor role "
                            + applicableTo.get() + " <yellow>not in " + handlerEntry.allowedTargets + "<yellow>");
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void invokeHandler(MxBaseHandlerEntry<MxGameEvent<S>, ?> entry, MxBaseContext<MxGameEvent<S>> ctx) {
        try {
            int verdictsBefore = ctx.getSubmittedVerdicts().size();
            ctx.trace("<yellow>[<green>RUN<yellow>]  " + entry.name + " (priority=" + entry.priority + ")");
            ((MxEventHandler) entry.handler).handle((MxEventContext) ctx);
            var verdicts = ctx.getSubmittedVerdicts();
            if (verdicts.size() > verdictsBefore) {
                MxCancellationState submitted = verdicts.get(verdicts.size() - 1);
                ctx.trace("<yellow>  <dark_gray>↳ <yellow>Verdict: " + submitted);
            }
        } catch (Exception e) {
            MxLogger.logMessage(MxLogLevel.ERROR,
                    "[MxGameEventBus] Handler '" + entry.name + "' threw an exception: " + e.getMessage());
        }
    }

    @Override
    protected MxConfigFileType loggedEventsConfigFile() {
        return configFile;
    }

    @Override
    protected String loggedEventsConfigKey() {
        return "logged-events";
    }

    @Override
    protected String busLabel() {
        return "MxGameEventBus";
    }
}


