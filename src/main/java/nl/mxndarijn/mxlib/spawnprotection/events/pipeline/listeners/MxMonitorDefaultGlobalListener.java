package nl.mxndarijn.mxlib.spawnprotection.events.pipeline.listeners;

import nl.mxndarijn.mxlib.mxeventbus.core.MxPriority;
import nl.mxndarijn.mxlib.mxeventbus.core.MxSubscribe;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventContext;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxGlobalEventListener;
import nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events.*;

/**
 * Collects all default MONITOR handlers for the global (non-game) event pipeline.
 *
 * <p>Each handler here is a pure pass-through: if {@code ctx.isCancelled()} is {@code true},
 * the underlying Paper event is cancelled. This mirrors the role of
 * {@code MonitorDefaultListener} in the game pipeline.</p>
 *
 * <p>Non-cancellable events (join, quit, advancement) are handled with their own
 * post-dispatch logic rather than a cancellation flag.</p>
 */
public class MxMonitorDefaultGlobalListener extends MxGlobalEventListener {

    /**
     * Constructs a new {@code MonitorDefaultGlobalListener}.
     */
    public MxMonitorDefaultGlobalListener() {
        super();
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.block.BlockBreakEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onBlockBreak(MxGlobalEventContext<MxSpawnBlockBreakEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.block.BlockPlaceEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onBlockPlace(MxGlobalEventContext<MxSpawnBlockPlaceEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.entity.EntityDamageEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onEntityDamage(MxGlobalEventContext<MxSpawnEntityDamageEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.entity.EntityDamageByEntityEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onEntityDamageByEntity(MxGlobalEventContext<MxSpawnEntityDamageByEntityEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.player.PlayerDropItemEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onPlayerDropItem(MxGlobalEventContext<MxSpawnPlayerDropItemEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.entity.EntityPickupItemEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onEntityPickupItem(MxGlobalEventContext<MxSpawnEntityPickupItemEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.inventory.InventoryClickEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onInventoryClick(MxGlobalEventContext<MxSpawnInventoryClickEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.entity.FoodLevelChangeEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onFoodLevelChange(MxGlobalEventContext<MxSpawnFoodLevelChangeEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.player.PlayerInteractEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onPlayerInteract(MxGlobalEventContext<MxSpawnPlayerInteractEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.player.PlayerPortalEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onPlayerPortal(MxGlobalEventContext<MxSpawnPlayerPortalEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying
     * {@link org.bukkit.event.player.PlayerArmorStandManipulateEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onArmorStandManipulate(MxGlobalEventContext<MxSpawnPlayerArmorStandManipulateEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.entity.EntityChangeBlockEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onEntityChangeBlock(MxGlobalEventContext<MxSpawnEntityChangeBlockEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.entity.EntitySpawnEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onEntitySpawn(MxGlobalEventContext<MxSpawnEntitySpawnEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onSignChangeMonitor(MxGlobalEventContext<MxSpawnSignChangeEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }

    /**
     * Applies cancellation to the underlying {@link org.bukkit.event.player.PlayerMoveEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onPlayerMove(MxGlobalEventContext<MxSpawnPlayerMoveEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }
    /**
     * Applies cancellation to the underlying {@link io.papermc.paper.event.player.AsyncChatEvent}.
     *
     * @param ctx the event context carrying the cancellation verdict
     */
    @MxSubscribe(priority = MxPriority.MONITOR)
    public void onChat(MxGlobalEventContext<MxGlobalChatEvent, MxWorldType> ctx) {
        if (ctx.isCancelled()) {
            ctx.event().getPaperEvent().setCancelled(true);
        }
    }
}




