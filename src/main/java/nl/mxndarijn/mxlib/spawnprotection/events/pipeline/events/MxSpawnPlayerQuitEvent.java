package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Fired when a player leaves the server.
 *
 * <p>The underlying {@link PlayerQuitEvent} is exposed so handlers can clean up player state.</p>
 */
public final class MxSpawnPlayerQuitEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerQuitEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerQuitEvent}.
     *
     * @param player     the player who left; must not be {@code null}
     * @param paperEvent the underlying Bukkit player quit event; must not be {@code null}
     */
    public MxSpawnPlayerQuitEvent(Player player, PlayerQuitEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who left.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit player quit event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerQuitEvent getPaperEvent() {
        return paperEvent;
    }
}




