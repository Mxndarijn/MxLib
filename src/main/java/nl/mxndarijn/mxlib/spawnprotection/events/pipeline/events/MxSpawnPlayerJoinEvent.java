package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import lombok.Getter;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Fired when a player joins the server.
 *
 * <p>The underlying {@link PlayerJoinEvent} is exposed so handlers can react to the join.</p>
 */
@Getter
public final class MxSpawnPlayerJoinEvent extends MxGlobalEvent<MxWorldType> {

    /**
     * -- GETTER --
     *  Returns the player who joined.
     *
     * @return the player; never {@code null}
     */
    private final Player player;
    /**
     * -- GETTER --
     *  Returns the underlying Bukkit player join event.
     *
     * @return the paper event; never {@code null}
     */
    private final PlayerJoinEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerJoinEvent}.
     *
     * @param player     the player who joined; must not be {@code null}
     * @param paperEvent the underlying Bukkit player join event; must not be {@code null}
     */
    public MxSpawnPlayerJoinEvent(Player player, PlayerJoinEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

}




