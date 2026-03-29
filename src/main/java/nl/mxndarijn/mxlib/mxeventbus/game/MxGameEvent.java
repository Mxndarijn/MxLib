package nl.mxndarijn.mxlib.mxeventbus.game;
import nl.mxndarijn.mxlib.mxeventbus.core.MxBaseEvent;

/**
 * Base class for all game events in the annotation-driven event pipeline.
 *
 * <p>Each event captures the game state (of type {@code S}) at the moment it is created.
 * Guards in the pipeline use this snapshot to decide whether a handler should run.</p>
 *
 * @param <S> the concrete game-state type, which must implement {@link MxIGameState}
 */
public abstract class MxGameEvent<S extends MxIGameState> implements MxBaseEvent {

    private final S gameState;

    /**
     * Constructs a new game event with the given game state snapshot.
     *
     * @param gameState the current game state at the time the event is created; must not be {@code null}
     */
    protected MxGameEvent(S gameState) {
        if (gameState == null) throw new IllegalArgumentException("gameState must not be null");
        this.gameState = gameState;
    }

    /**
     * Returns the game state captured when this event was created.
     * Guards use this value to decide whether a handler should run.
     *
     * @return the game state; never {@code null}
     */
    public S gameState() {
        return gameState;
    }
}


