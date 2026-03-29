package nl.mxndarijn.mxlib.mxeventbus.game;

/**
 * Marker interface for world-type categorisation used by the global event bus pipeline.
 *
 * <p>Implementations (typically enums) classify a Minecraft world by its role in the
 * plugin, allowing {@code MxAbstractEventBus} subclasses to filter handlers by world
 * category without depending on any specific world-type enum.</p>
 */
public interface MxIWorldType {
}
