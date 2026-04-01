package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;

/**
 * Fired when a player attempts to travel through a portal in any non-game world.
 *
 * <p>The underlying {@link PlayerPortalEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnPlayerPortalEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerPortalEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerPortalEvent}.
     *
     * @param player     the player entering the portal; must not be {@code null}
     * @param paperEvent the underlying Bukkit player portal event; must not be {@code null}
     */
    public MxSpawnPlayerPortalEvent(Player player, PlayerPortalEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player entering the portal.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit player portal event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerPortalEvent getPaperEvent() {
        return paperEvent;
    }
}




