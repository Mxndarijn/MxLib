package nl.mxndarijn.mxlib.mxeventbus.global;

import nl.mxndarijn.mxlib.mxeventbus.game.MxIWorldType;

/**
 * Categorises a Minecraft world by its role in the plugin.
 *
 * <p>Used by the {@link MxWorldTypes} annotation to restrict a {@link MxGlobalEventBus}
 * handler to events that originate from a specific category of world.</p>
 *
 * <ul>
 *   <li>{@link #SPAWN} — the central lobby/spawn world players return to between games.</li>
 *   <li>{@link #PRESET} — a preset/template world used for map configuration.</li>
 *   <li>{@link #MAP} — an active game-map world (loaded per game instance).</li>
 *   <li>{@link #OTHER} — any other world (e.g. nether, end, custom utility worlds).</li>
 * </ul>
 */
public enum MxWorldType implements MxIWorldType {

    /** The central lobby/spawn world. */
    SPAWN,

    /** A preset or template world used for map configuration. */
    PRESET,

    /** An active game-map world loaded for a running game. */
    MAP,

    /** Any other world not covered by the categories above (nether, end, etc.). */
    OTHER,

    /** If the world is game, this event will not be processed */
    GAME
}




