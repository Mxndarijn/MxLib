package nl.mxndarijn.mxlib.mxeventbus.game;

/**
 * Marker interface for actor-role enums used as guards in the event pipeline.
 *
 * <p>Implement this interface on any enum that represents the role of an actor
 * (e.g. {@code PLAYER}, {@code HOST}, {@code SPECTATOR}) so that the generic
 * event bus can filter handlers without depending on a concrete WIDM enum.</p>
 */
public interface MxIApplicableTo {
}

