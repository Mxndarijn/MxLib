package nl.mxndarijn.mxlib.spawnprotection.spawn;

import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.mxeventbus.core.MxCancellationState;
import nl.mxndarijn.mxlib.mxeventbus.core.MxPriority;
import nl.mxndarijn.mxlib.mxeventbus.core.MxSubscribe;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventContext;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.mxscoreboard.MxSupplierScoreBoard;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxGlobalEventListener;
import nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events.MxSpawnPlayerAdvancementDoneEvent;
import nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events.MxSpawnPlayerJoinEvent;
import nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events.MxSpawnPlayerQuitEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Handles player join and quit lifecycle events for the spawn world via the
 * {@link nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventBus}.
 *
 * <p>On join: creates and registers the player's spawn scoreboard, removes them from any
 * game queues they were in, and teleports them to spawn if they are not in a game world.
 * On quit: removes the player from any game queues and cleans up their scoreboard.
 * Also suppresses advancement announcement messages server-wide.</p>
 *
 * <p>All WIDM-specific logic (scoreboard creation, queue management, game-world checks)
 * is delegated to the injected {@link MxISpawnLifecycleProvider}.</p>
 */
public final class MxSpawnPlayerLifecycleListener extends MxGlobalEventListener {

    /** Spawn scoreboards keyed by player UUID. */
    private final HashMap<UUID, MxSupplierScoreBoard> scoreboards;
    private final MxISpawnLifecycleProvider provider;

    /**
     * Constructs a new {@code MxSpawnPlayerLifecycleListener}.
     *
     * @param scoreboards the shared scoreboard map owned by the spawn manager
     * @param provider    the {@link MxISpawnLifecycleProvider} supplying lifecycle dependencies
     */
    public MxSpawnPlayerLifecycleListener(HashMap<UUID, MxSupplierScoreBoard> scoreboards,
                                        MxISpawnLifecycleProvider provider) {
        this.scoreboards = scoreboards;
        this.provider = provider;
    }

    /**
     * Handles a player joining the server. Creates and registers their spawn scoreboard,
     * then teleports them to spawn if they are not already in a game world.
     * This handler runs for all world types since join events have no meaningful world context yet.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerJoinEvent}
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void initializeJoiningPlayer(MxGlobalEventContext<MxSpawnPlayerJoinEvent, MxWorldType> ctx) {
        Player player = ctx.event().getPlayer();
        if(ctx.isCancelled())
            return;

        MxSupplierScoreBoard sb = provider.createScoreboard(player);
        scoreboards.put(player.getUniqueId(), sb);

        World spawn = provider.getSpawnWorld();
        if (player.getWorld() == spawn) {
            provider.callPlayerChangedWorldEvent(player, player.getWorld());
        }

        provider.teleportToSpawn(player);
    }

    @MxSubscribe
    public void cancelPlayerLoadSpawnIfPlayerIsInGame(MxGlobalEventContext<MxSpawnPlayerJoinEvent, MxWorldType> ctx) {
        Player player = ctx.event().getPlayer();

        if (provider.isPlayerInGame(player.getUniqueId())) {
            ctx.submitVerdict(MxCancellationState.HARD_DENY);
        }
    }

    /**
     * Handles a player leaving the server. Removes the player from any game queues
     * they are in and cleans up their scoreboard.
     * This handler runs for all world types since quit events fire regardless of world.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerQuitEvent}
     */
    @MxSubscribe
    public void cleanupQuittingPlayer(MxGlobalEventContext<MxSpawnPlayerQuitEvent, MxWorldType> ctx) {
        UUID uuid = ctx.event().getPlayer().getUniqueId();
        provider.removePlayerFromAllQueues(uuid);
        MxSupplierScoreBoard sb = scoreboards.remove(uuid);
        if (sb != null) sb.delete();
    }

    /**
     * Suppresses advancement announcement messages server-wide by setting the message to {@code null}.
     * This handler runs for all world types.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerAdvancementDoneEvent}
     */
    @MxSubscribe
    public void suppressAdvancementAnnouncement(MxGlobalEventContext<MxSpawnPlayerAdvancementDoneEvent, MxWorldType> ctx) {
        ctx.event().getPaperEvent().message(null);
    }
}

