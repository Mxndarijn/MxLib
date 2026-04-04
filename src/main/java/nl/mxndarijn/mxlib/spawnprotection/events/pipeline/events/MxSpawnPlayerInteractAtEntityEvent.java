package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events;


import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxSpawnEventRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

/**
 * Fired when a player right-clicks (interacts at) an entity in any non-game world.
 *
 * <p>The underlying {@link PlayerInteractAtEntityEvent} is exposed so handlers can cancel it.</p>
 */
public final class MxSpawnPlayerInteractAtEntityEvent extends MxGlobalEvent<MxWorldType> {

    private final Player player;
    private final PlayerInteractAtEntityEvent paperEvent;

    /**
     * Constructs a new {@code MxSpawnPlayerInteractAtEntityEvent}.
     *
     * @param player     the player who interacted; must not be {@code null}
     * @param paperEvent the underlying Bukkit event; must not be {@code null}
     */
    public MxSpawnPlayerInteractAtEntityEvent(Player player, PlayerInteractAtEntityEvent paperEvent) {
        super(MxSpawnEventRegistry.getWorldTypeResolver().resolve(player.getWorld()));
        this.player = player;
        this.paperEvent = paperEvent;
    }

    /**
     * Returns the player who interacted.
     *
     * @return the player; never {@code null}
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the underlying Bukkit player interact-at-entity event.
     *
     * @return the paper event; never {@code null}
     */
    public PlayerInteractAtEntityEvent getPaperEvent() {
        return paperEvent;
    }
}
