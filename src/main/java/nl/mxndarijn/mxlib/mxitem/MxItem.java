package nl.mxndarijn.mxlib.mxitem;

import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.chatprefix.MxChatPrefixManager;
import nl.mxndarijn.mxlib.chatprefix.MxStandardChatPrefix;
import nl.mxndarijn.mxlib.language.MxLanguageManager;
import nl.mxndarijn.mxlib.language.MxStandardLanguageText;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.util.MxFunctions;
import nl.mxndarijn.mxlib.util.MxMessageUtil;
import nl.mxndarijn.mxlib.util.MxWorldFilter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;

/**
 * Base item with generic policy hooks for execution control.
 * Projects can subclass and override {@link #canExecute(Player, ItemStack)}
 * or the event-specific variants to enforce custom rules.
 *
 * <p>Interact events are handled via {@link MxInteractPipeline} so that
 * right-clicking air (which the server auto-cancels) is still processed correctly.
 */
public abstract class MxItem<T extends MxItemContext> implements Listener {

    /** The PDC key used to store the unique item tag. */
    public static final String TAG_KEY = "mxitem_id";

    public final JavaPlugin plugin;
    private final ItemStack is;
    private final MxWorldFilter worldFilter;
    private final MxLanguageManager languageManager;
    private final Action[] actions;
    private final String itemTag;
    private final NamespacedKey namespacedKey;

    public MxItem(ItemStack is, MxWorldFilter worldFilter, Action... actions) {
        this.worldFilter = worldFilter;
        this.languageManager = MxLanguageManager.getInstance();
        this.actions = actions;

        this.plugin = MxLib.getPlugin();
        this.namespacedKey = new NamespacedKey(this.plugin, TAG_KEY);

        // Stamp a stable, class-derived tag onto the item so we can identify it by tag, not by type/name.
        // Using the fully-qualified class name ensures the tag is unique per subclass and persistent across restarts.
        this.itemTag = this.getClass().getName();
        this.is = is;

        // Check if the item already has the tag; if not, log an error and stamp it now.
        ItemMeta meta = is.getItemMeta();
        if (meta == null) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXITEM,
                    "ItemStack has no ItemMeta — cannot stamp MxItem tag for: " + this.getClass().getName());
        } else {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (!pdc.has(namespacedKey, PersistentDataType.STRING)) {
                MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXITEM,
                        "ItemStack is missing MxItem tag, stamping it now for: " + this.getClass().getName());
                pdc.set(namespacedKey, PersistentDataType.STRING, this.itemTag);
                is.setItemMeta(meta);
            }
        }

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        // MxSubscribe to the separate interact pipeline instead of relying on ignoreCancelled,
        // because right-clicking air is auto-cancelled by the server.
        MxInteractPipeline.getInstance().subscribe(this::handleMxInteract, MxInteractPriority.HIGH);
    }

    /** Compares by the unique MxItem tag stored in the item's PersistentDataContainer. */
    public boolean isItemTheSame(ItemStack item) {

        //New based checking based on nbt data
        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if(itemTag.equals(pdc.get(namespacedKey, PersistentDataType.STRING)))
            return true;

        //Legacy Based checking
        if (item.getType() != is.getType()) return false;

        if (is.getItemMeta().hasDisplayName() && item.getItemMeta().hasDisplayName()) {
            return MxFunctions.convertComponentToString(item.getItemMeta().displayName())
                .equalsIgnoreCase(MxFunctions.convertComponentToString(is.getItemMeta().displayName()));
        }
        return !is.getItemMeta().hasDisplayName();


    }

    /**
     * Called by {@link MxInteractPipeline} for every {@link PlayerInteractEvent},
     * regardless of whether Bukkit has already cancelled it.
     */
    private void handleMxInteract(MxPlayerInteractEvent mxEvent) {
        if (mxEvent.isCancelled()) return;

        PlayerInteractEvent e = mxEvent.getBukkitEvent();
        if (Arrays.stream(actions).noneMatch(a -> a == e.getAction())) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack used = e.getItem();
        if (used == null || !used.hasItemMeta() || used.getType() == Material.AIR) return;

        Player p = e.getPlayer();

        if (!isItemTheSame(used)) return;

        T context = createContext();

        if (!canExecuteInteract(p, used, e, context)) {
            return;
        }

        if(mxEvent.isCancelled()) {
            mxEvent.getBukkitEvent().setCancelled(true);
        }

        try {
            execute(p, e, context);
        } catch (Exception ex) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXITEM,
                    "Could not execute item: " + MxFunctions.convertComponentToString(is.getItemMeta().displayName()));
            ex.printStackTrace();
            MxMessageUtil.sendMessageToPlayer(p,
                    languageManager.getLanguageString(
                            MxStandardLanguageText.ERROR_WHILE_EXECUTING_ITEM,
                            Collections.emptyList(),
                            MxChatPrefixManager.getInstance().requireFind(MxStandardChatPrefix.DEFAULT)));
        }
    }

    @EventHandler
    public void onPlaceBlockWithItem(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (!inHand.hasItemMeta() || inHand.getType() == Material.AIR) return;

        if (!isItemTheSame(inHand)) return;

        // Generic, overridable policy hook
        if (!canPlaceItem(p, inHand, e)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreakBlockWithItem(BlockBreakEvent e) {
        Player p = e.getPlayer();

        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (!inHand.hasItemMeta() || inHand.getType() == Material.AIR) return;

        if (!isItemTheSame(inHand)) return;

        // Generic, overridable policy hook
        if (!canExecuteBreak(p, inHand, e)) {
            e.setCancelled(true);
            return;
        }

        if (worldFilter != null) {
            if (!worldFilter.isPlayerInCorrectWorld(p)) {
                MxMessageUtil.sendMessageToPlayer(p,
                        languageManager.getLanguageString(
                                MxStandardLanguageText.NOT_CORRECT_WORLD,
                                Collections.emptyList(),
                                MxChatPrefixManager.getInstance().requireFind(MxStandardChatPrefix.DEFAULT)));
                return;
            }
        }

        try {
            executeOnBreak(p, e);
        } catch (Exception ex) {
            MxLogger.logMessage(MxLogLevel.ERROR, MxStandardPrefix.MXITEM,
                    "Could not execute item: " + MxFunctions.convertComponentToString(is.getItemMeta().displayName()));
            ex.printStackTrace();
            MxMessageUtil.sendMessageToPlayer(p,
                    languageManager.getLanguageString(
                            MxStandardLanguageText.ERROR_WHILE_EXECUTING_ITEM,
                            Collections.emptyList(),
                            MxChatPrefixManager.getInstance().requireFind(MxStandardChatPrefix.DEFAULT)));
        }
    }

    /**
     * Generic policy hook for execution checks.
     * Default returns true; override in project code as needed.
     */
    protected boolean canExecute(Player player, ItemStack item) {
        return true;
    }

    /**
     * Event-specific hook for interact events.
     * Default delegates to {@link #canExecute(Player, ItemStack)}.
     */
    protected boolean canExecuteInteract(Player player, ItemStack item, PlayerInteractEvent event, T context) {
        return canExecute(player, item);
    }

    /**
     * Event-specific hook for block-break events.
     * Default delegates to {@link #canExecute(Player, ItemStack)}.
     */
    protected boolean canExecuteBreak(Player player, ItemStack item, BlockBreakEvent event) {
        return canExecute(player, item);
    }

    /**
     * Event-specific hook for block-place events.
     * Default delegates to {@link #canExecute(Player, ItemStack)}.
     */
    protected boolean canPlaceItem(Player player, ItemStack item, BlockPlaceEvent event) {
        return canExecute(player, item);
    }

    public abstract void execute(Player p, PlayerInteractEvent e, T context);

    public abstract void executeOnBreak(Player p, BlockBreakEvent e);

    public abstract T createContext();
}
