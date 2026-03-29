package nl.mxndarijn.mxlib.mxeventbus.core;

/**
 * Marker interface shared by all events that travel through an {@link MxAbstractEventBus}.
 *
 * <p>Concrete event types implement this interface so that {@link MxAbstractEventBus}
 * can be parameterised over a single event hierarchy.</p>
 */
public interface MxBaseEvent {
}


