package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import io.papermc.paper.event.player.AsyncChatEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.entity.Player;

/**
 * Fired when a player sends a chat message outside of a game world.
 *
 * <p>The underlying Paper {@link AsyncChatEvent} is exposed so handlers can
 * cancel it, modify the message renderer, or adjust the recipient set.</p>
 *
 * <p>The {@link MxWorldType} is resolved
 * automatically from the player's current world at construction time.</p>
 */
public final class MxGlobalChatEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final AsyncChatEvent paperEvent;

    /**
     * Constructs a new {@code MxGlobalChatEvent}.
     *
     * @param player     the player who sent the message; must not be {@code null}
     * @param paperEvent the underlying Paper async chat event; must not be {@code null}
     */
    public MxGlobalChatEvent(Player player, AsyncChatEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        if (player == null) throw new IllegalArgumentException("player must not be null");
        if (paperEvent == null) throw new IllegalArgumentException("paperEvent must not be null");
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who sent the message.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Paper async chat event.
     * Handlers may cancel it or modify the message renderer via this reference.
     *
     * @return the Paper event; never {@code null}
     */
    public AsyncChatEvent getPaperEvent() {
        return paperEvent;
    }
}




