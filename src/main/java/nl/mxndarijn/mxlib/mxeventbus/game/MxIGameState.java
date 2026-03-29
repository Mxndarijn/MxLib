package nl.mxndarijn.mxlib.mxeventbus.game;

/**
 * Marker interface for game-state enums used as guards in the event pipeline.
 *
 * <p>Implement this interface on any enum that represents the lifecycle state of a game
 * (e.g. {@code WAITING}, {@code PLAYING}, {@code FINISHED}) so that the generic
 * {@link MxGameEvent} and {@link MxGameEventBus} can operate without depending on a
 * concrete WIDM enum.</p>
 */
public interface MxIGameState {
}

