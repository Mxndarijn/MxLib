package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.wieisdemol.readytomove.events.base.SpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

/**
 * Fired when a player attempts to manipulate an armor stand in any non-game world.
 *
 * <p>The underlying {@link PlayerArmorStandManipulateEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnPlayerArmorStandManipulateEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerArmorStandManipulateEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerArmorStandManipulateEvent}.
     *
     * @param player     the player manipulating the armor stand; must not be {@code null}
     * @param paperEvent the underlying Bukkit armor stand manipulate event; must not be {@code null}
     */
    public MxSpawnPlayerArmorStandManipulateEvent(Player player, PlayerArmorStandManipulateEvent paperEvent) {
        super(SpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player manipulating the armor stand.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit armor stand manipulate event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerArmorStandManipulateEvent getPaperEvent() {
        return paperEvent;
    }
}




