package nl.mxndarijn.mxlib.spawnprotection.spawn;

import nl.mxndarijn.mxlib.mxeventbus.core.MxCancellationState;
import nl.mxndarijn.mxlib.mxeventbus.core.MxSubscribe;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventContext;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldTypes;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxGlobalEventListener;
import nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

/**
 * Handles all spawn-world protection rules via the {@link nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventBus}.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Cancel block breaking and placing for players not in modify mode.</li>
 *   <li>Teleport players back to spawn when they fall below Y = -50 (void rescue).</li>
 *   <li>Cancel all entity damage and PvP in the spawn world.</li>
 *   <li>Cancel item dropping and pickup for players without the appropriate permissions.</li>
 *   <li>Cancel inventory clicks on the player's own inventory without permission.</li>
 *   <li>Prevent hunger loss.</li>
 *   <li>Cancel flower pot and potted-plant interactions to prevent stealing the planted item.</li>
 *   <li>Cancel interactions with inventory-opening blocks; allow openable blocks but schedule
 *       a state restore via {@link MxBlockRestoreService}.</li>
 *   <li>Cancel portal travel, armor stand manipulation, and farmland trampling.</li>
 *   <li>Cancel sign editing for players not in spawn-modify mode.</li>
 * </ul>
 *
 * <p>All WIDM-specific logic (permissions, block sets, modify-mode checks, teleportation)
 * is delegated to the injected {@link MxISpawnProtectionProvider}.</p>
 */
public final class MxSpawnProtectionListener extends MxGlobalEventListener {

    private final MxISpawnProtectionProvider provider;
    private final MxBlockRestoreService blockRestoreService;

    /**
     * Constructs a new {@code MxSpawnProtectionListener}.
     *
     * @param provider            the {@link MxISpawnProtectionProvider} supplying protection rules
     * @param blockRestoreService the {@link MxBlockRestoreService} used to schedule gate/door restores
     */
    public MxSpawnProtectionListener(MxISpawnProtectionProvider provider, MxBlockRestoreService blockRestoreService) {
        this.provider = provider;
        this.blockRestoreService = blockRestoreService;
    }

    /**
     * Cancels block breaking in the spawn world unless the player has spawn-modify mode active.
     *
     * @param ctx the event context wrapping a {@link MxSpawnBlockBreakEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelBlockBreak(MxGlobalEventContext<MxSpawnBlockBreakEvent, MxWorldType> ctx) {
        if (provider.isInModifyMode(ctx.event().getPlayer())) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels block placing in the spawn world unless the player has spawn-modify mode active.
     *
     * @param ctx the event context wrapping a {@link MxSpawnBlockPlaceEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelBlockPlace(MxGlobalEventContext<MxSpawnBlockPlaceEvent, MxWorldType> ctx) {
        if (provider.isInModifyMode(ctx.event().getPlayer())) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Teleports a player back to spawn if they fall below Y = -50 (void rescue).
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerMoveEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void rescuePlayerFromVoid(MxGlobalEventContext<MxSpawnPlayerMoveEvent, MxWorldType> ctx) {
        Player player = ctx.event().getPlayer();
        if (player.getLocation().getY() < -50) {
            provider.teleportToSpawn(player);
        }
    }

    /**
     * Cancels all entity damage in the spawn world, making it a safe zone.
     *
     * @param ctx the event context wrapping a {@link MxSpawnEntityDamageEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelEntityDamage(MxGlobalEventContext<MxSpawnEntityDamageEvent, MxWorldType> ctx) {
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels players hitting or attacking any entity in the spawn world.
     * Players in modify mode are exempt.
     *
     * @param ctx the event context wrapping a {@link MxSpawnEntityDamageByEntityEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelEntityDamageByEntity(MxGlobalEventContext<MxSpawnEntityDamageByEntityEvent, MxWorldType> ctx) {
        if (ctx.event().getPaperEvent().getDamager() instanceof Player player
                && provider.isInModifyMode(player)) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels item dropping in the spawn world for players without the drop-item permission.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerDropItemEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelItemDrop(MxGlobalEventContext<MxSpawnPlayerDropItemEvent, MxWorldType> ctx) {
        Player player = ctx.event().getPlayer();
        if (provider.isInModifyMode(player)) return;
        if (!provider.hasDropItemPermission(player))
            ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels item pickup in the spawn world for entities without the pickup-item permission.
     *
     * @param ctx the event context wrapping a {@link MxSpawnEntityPickupItemEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelItemPickup(MxGlobalEventContext<MxSpawnEntityPickupItemEvent, MxWorldType> ctx) {
        var entity = ctx.event().getPaperEvent().getEntity();
        if (entity instanceof Player player && provider.isInModifyMode(player)) return;
        if (!provider.hasPickupItemPermission(entity))
            ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels inventory clicks on the player's own inventory in the spawn world
     * for players without the change-inventory permission.
     *
     * @param ctx the event context wrapping a {@link MxSpawnInventoryClickEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelUnauthorizedInventoryClick(MxGlobalEventContext<MxSpawnInventoryClickEvent, MxWorldType> ctx) {
        var e = ctx.event().getPaperEvent();
        if (e.getWhoClicked() instanceof Player whoClicked && provider.isInModifyMode(whoClicked)) return;
        if (e.getClickedInventory() == e.getWhoClicked().getInventory()) {
            if (!provider.hasChangeInventoryPermission((Player) e.getWhoClicked()))
                ctx.submitVerdict(MxCancellationState.HARD_DENY);
        }
    }

    /**
     * Prevents hunger loss in the spawn world by keeping the food level at 20.
     *
     * @param ctx the event context wrapping a {@link MxSpawnFoodLevelChangeEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void preventHungerLoss(MxGlobalEventContext<MxSpawnFoodLevelChangeEvent, MxWorldType> ctx) {
        ctx.event().getPaperEvent().setFoodLevel(20);
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Handles player block interactions in the spawn world.
     * Cancels interactions with flower pots, decorated pots, and all potted-plant variants (prevents stealing the planted item).
     * Cancels interactions with inventory-opening blocks entirely.
     * Allows interactions with openable blocks (gates, doors, trapdoors) but schedules
     * a state restore via {@link MxBlockRestoreService} after 2 minutes.
     * Players in modify mode bypass all restrictions; any pending restore for a block
     * they interact with is cancelled to make the change permanent.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerInteractEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void enforceInteractionRules(MxGlobalEventContext<MxSpawnPlayerInteractEvent, MxWorldType> ctx) {
        var e = ctx.event().getPaperEvent();
        Block block = e.getClickedBlock();
        if (block == null) return;
        Material type = block.getType();

        if (provider.isInModifyMode(ctx.event().getPlayer())) {
            if (provider.getOpenableBlocks().contains(type)) {
                blockRestoreService.cancelRestore(block);
                Block otherHalf = getDoorOtherHalf(block);
                if (otherHalf != null) blockRestoreService.cancelRestore(otherHalf);
            }
            return;
        }

        if (type == Material.FLOWER_POT || type == Material.DECORATED_POT || type.name().startsWith("POTTED_")) {
            ctx.submitVerdict(MxCancellationState.HARD_DENY);
            return;
        }

        if (provider.getInventoryOpeningBlocks().contains(type)) {
            ctx.submitVerdict(MxCancellationState.HARD_DENY);
            return;
        }

        if (provider.getOpenableBlocks().contains(type)) {
            BlockData originalData = block.getBlockData().clone();
            Block otherHalf = getDoorOtherHalf(block);
            BlockData otherOriginalData = otherHalf != null ? otherHalf.getBlockData().clone() : null;
            // Run on next tick so the block state has already changed before we capture it
            provider.runTask(() -> {
                blockRestoreService.scheduleRestore(block, originalData);
                if (otherHalf != null && otherOriginalData != null) {
                    blockRestoreService.scheduleRestore(otherHalf, otherOriginalData);
                }
            });
        }
    }

    /**
     * Returns the other half of a two-block-tall door, or {@code null} if the block is not a door.
     *
     * @param block the door block (either top or bottom half)
     * @return the adjacent door half block, or {@code null} if not applicable
     */
    private Block getDoorOtherHalf(Block block) {
        if (!(block.getBlockData() instanceof Bisected bisected)) return null;
        return bisected.getHalf() == Bisected.Half.BOTTOM
                ? block.getRelative(BlockFace.UP)
                : block.getRelative(BlockFace.DOWN);
    }

    /**
     * Cancels portal travel (nether/end) for players in the spawn world.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerPortalEvent}
     */
    @MxSubscribe
    @MxWorldTypes({MxWorldType.SPAWN, MxWorldType.MAP, MxWorldType.PRESET})
    public void cancelPortalTravel(MxGlobalEventContext<MxSpawnPlayerPortalEvent, MxWorldType> ctx) {
        if (provider.isInModifyMode(ctx.event().getPlayer())) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels armor stand manipulation in the spawn world.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerArmorStandManipulateEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelArmorStandManipulation(MxGlobalEventContext<MxSpawnPlayerArmorStandManipulateEvent, MxWorldType> ctx) {
        if (provider.isInModifyMode(ctx.event().getPlayer())) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels entity spawning (mobs, minecarts, boats, etc.) in the spawn world
     * unless at least one player with spawn-modify mode active is present.
     * Armor stands are always allowed to spawn so that hologram decorations work correctly.
     *
     * @param ctx the event context wrapping a {@link MxSpawnEntitySpawnEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelEntitySpawn(MxGlobalEventContext<MxSpawnEntitySpawnEvent, MxWorldType> ctx) {
        if (ctx.event().getPaperEvent().getEntity() instanceof ArmorStand) return;
        boolean anyModifyPlayer = ctx.event().getPaperEvent().getEntity().getWorld()
                .getPlayers().stream()
                .anyMatch(provider::isInModifyMode);
        if (anyModifyPlayer) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels farmland trampling in the spawn world, preserving planted crops.
     *
     * @param ctx the event context wrapping a {@link MxSpawnEntityChangeBlockEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelFarmlandTrample(MxGlobalEventContext<MxSpawnEntityChangeBlockEvent, MxWorldType> ctx) {
        if (ctx.event().getPaperEvent().getBlock().getType() == Material.FARMLAND) {
            ctx.submitVerdict(MxCancellationState.HARD_DENY);
        }
    }

    /**
     * Cancels turtle egg trampling (via physical player interaction) in the spawn world
     * unless the player has spawn-modify mode active.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerInteractEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelTurtleEggTrample(MxGlobalEventContext<MxSpawnPlayerInteractEvent, MxWorldType> ctx) {
        if (ctx.event().getPaperEvent().getAction() != Action.PHYSICAL) return;
        if (ctx.event().getPaperEvent().getClickedBlock() == null) return;
        if (ctx.event().getPaperEvent().getClickedBlock().getType() != Material.TURTLE_EGG) return;
        if (provider.isInModifyMode(ctx.event().getPlayer())) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels sign editing (clicking to edit a sign) in the spawn world unless the player
     * has spawn-modify mode active.
     *
     * @param ctx the event context wrapping a {@link MxSpawnSignChangeEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelSignEdit(MxGlobalEventContext<MxSpawnSignChangeEvent, MxWorldType> ctx) {
        if (provider.isInModifyMode(ctx.event().getPlayer())) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }

    /**
     * Cancels placing boats, minecarts, and spawn eggs in the spawn world unless the player
     * has spawn-modify mode active.
     *
     * @param ctx the event context wrapping a {@link MxSpawnPlayerInteractEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void cancelVehicleOrSpawnEggPlace(MxGlobalEventContext<MxSpawnPlayerInteractEvent, MxWorldType> ctx) {
        var e = ctx.event().getPaperEvent();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack item = e.getItem();
        if (item == null) return;
        String name = item.getType().name();
        if (!name.endsWith("_BOAT") && !name.endsWith("_CHEST_BOAT")
                && !name.endsWith("_MINECART") && !name.equals("MINECART")
                && !name.endsWith("_SPAWN_EGG")) return;
        if (provider.isInModifyMode(ctx.event().getPlayer())) return;
        ctx.submitVerdict(MxCancellationState.HARD_DENY);
    }
}

