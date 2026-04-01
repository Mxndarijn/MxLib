package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * Fired when a player completes an advancement in any non-game world.
 *
 * <p>The underlying {@link PlayerAdvancementDoneEvent} is exposed so handlers can suppress
 * the announcement message.</p>
 */
public final class MxSpawnPlayerAdvancementDoneEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerAdvancementDoneEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerAdvancementDoneEvent}.
     *
     * @param player     the player who completed the advancement; must not be {@code null}
     * @param paperEvent the underlying Bukkit advancement done event; must not be {@code null}
     */
    public MxSpawnPlayerAdvancementDoneEvent(Player player, PlayerAdvancementDoneEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who completed the advancement.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit advancement done event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerAdvancementDoneEvent getPaperEvent() {
        return paperEvent;
    }
}




