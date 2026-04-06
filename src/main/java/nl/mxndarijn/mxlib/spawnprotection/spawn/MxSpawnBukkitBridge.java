package nl.mxndarijn.mxlib.spawnprotection.spawn;

import io.papermc.paper.event.player.AsyncChatEvent;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventBus;
import nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

/**
 * Thin Bukkit {@link Listener} bridge that translates raw Bukkit events into
 * {@link nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEvent} instances and posts them
 * to the {@link MxGlobalEventBus}.
 *
 * <p>This class contains no business logic. All logic lives in the focused listener classes
 * ({@link MxSpawnProtectionListener}, {@link MxSpawnChatListener},
 * {@link MxSpawnPlayerLifecycleListener}) registered on the bus.</p>
 *
 * <p>Cancellation of underlying Bukkit events is handled exclusively by
 * {@link nl.mxndarijn.mxlib.spawnprotection.spawn.MxSpawnBukkitBridge}
 * in the MONITOR phase, which reads the resolved verdict from the context and calls
 * {@code getPaperEvent().setCancelled(true)} when denied.</p>
 */
public final class MxSpawnBukkitBridge implements Listener {
    @SuppressWarnings("rawtypes")
    private final MxGlobalEventBus bus;
    private final MxISpawnChatGuard chatGuard;

    /**
     * Constructs a new {@code MxSpawnBukkitBridge}.
     *
     * @param bus       the {@link MxGlobalEventBus} to post events to; must not be {@code null}
     * @param chatGuard the {@link MxISpawnChatGuard} used to skip players already in a game; must not be {@code null}
     */
    @SuppressWarnings("rawtypes")
    public MxSpawnBukkitBridge(MxGlobalEventBus bus, MxISpawnChatGuard chatGuard) {
        this.bus = bus;
        this.chatGuard = chatGuard;
    }

    /**
     * Translates a Bukkit {@link BlockBreakEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        bus.post(new MxSpawnBlockBreakEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link BlockPlaceEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        bus.post(new MxSpawnBlockPlaceEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link InventoryOpenEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        bus.post(new MxSpawnInventoryOpenEvent(p, e));
    }

    /**
     * Translates a Bukkit {@link PlayerMoveEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        bus.post(new MxSpawnPlayerMoveEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link EntityDamageEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        bus.post(new MxSpawnEntityDamageEvent(e));
    }

    /**
     * Translates a Bukkit {@link EntityDamageByEntityEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        bus.post(new MxSpawnEntityDamageByEntityEvent(e));
    }

    /**
     * Translates a Bukkit {@link PlayerDropItemEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        bus.post(new MxSpawnPlayerDropItemEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link EntityPickupItemEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onPickupItem(EntityPickupItemEvent e) {
        bus.post(new MxSpawnEntityPickupItemEvent(e));
    }

    /**
     * Translates a Bukkit {@link InventoryClickEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        bus.post(new MxSpawnInventoryClickEvent(e));
    }

    /**
     * Translates a Bukkit {@link FoodLevelChangeEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        bus.post(new MxSpawnFoodLevelChangeEvent(e));
    }

    /**
     * Translates a Bukkit {@link PlayerInteractEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        bus.post(new MxSpawnPlayerInteractEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link PlayerInteractAtEntityEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        bus.post(new MxSpawnPlayerInteractAtEntityEvent(player, e));
    }

    /**
     * Translates a Bukkit {@link PlayerPortalEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        bus.post(new MxSpawnPlayerPortalEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link PlayerArmorStandManipulateEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        bus.post(new MxSpawnPlayerArmorStandManipulateEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link EntityChangeBlockEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        bus.post(new MxSpawnEntityChangeBlockEvent(e));
    }

    /**
     * Translates a Bukkit {@link PlayerJoinEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        MxLogger.logMessage("Player " + e.getPlayer().getName() + " joined the server.: " + chatGuard.isPlayerInGame(e.getPlayer().getUniqueId()));
        bus.post(new MxSpawnPlayerJoinEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link PlayerQuitEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        bus.post(new MxSpawnPlayerQuitEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link PlayerAdvancementDoneEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        bus.post(new MxSpawnPlayerAdvancementDoneEvent(e.getPlayer(), e));
    }

    /**
     * Translates a Bukkit {@link EntitySpawnEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        bus.post(new MxSpawnEntitySpawnEvent(e));
    }

    /**
     * Translates a Bukkit {@link SignChangeEvent} and posts it to the bus.
     *
     * @param e the Bukkit event
     */
    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        bus.post(new MxSpawnSignChangeEvent(player, e));
    }

    /**
     * Translates a Bukkit {@link AsyncChatEvent} and posts it to the bus.
     * Skips players who are already in a game (they are handled by the game event pipeline).
     *
     * @param e the Bukkit async chat event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (chatGuard.isPlayerInGame(e.getPlayer().getUniqueId())) return;
        bus.post(new MxGlobalChatEvent(e.getPlayer(), e));
    }
}

