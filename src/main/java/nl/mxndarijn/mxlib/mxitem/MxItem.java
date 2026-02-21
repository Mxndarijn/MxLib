package nl.mxndarijn.mxlib.mxitem;

import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.chatprefix.ChatPrefixManager;
import nl.mxndarijn.mxlib.chatprefix.StandardChatPrefix;
import nl.mxndarijn.mxlib.inventory.saver.InventoryManager;
import nl.mxndarijn.mxlib.language.LanguageManager;
import nl.mxndarijn.mxlib.language.StandardLanguageText;
import nl.mxndarijn.mxlib.logger.LogLevel;
import nl.mxndarijn.mxlib.logger.Logger;
import nl.mxndarijn.mxlib.logger.StandardPrefix;
import nl.mxndarijn.mxlib.util.Functions;
import nl.mxndarijn.mxlib.util.MessageUtil;
import nl.mxndarijn.mxlib.util.MxWorldFilter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;

/**
 * Base item with generic policy hooks for execution control.
 * Projects can subclass and override {@link #canExecute(Player, ItemStack)}
 * or the event-specific variants to enforce custom rules.
 */
public abstract class MxItem<T extends MxItemContext> implements Listener {

    public final JavaPlugin plugin;
    private final ItemStack is;
    private final MxWorldFilter worldFilter;
    private final LanguageManager languageManager;
    private final Action[] actions;

    public MxItem(ItemStack is, MxWorldFilter worldFilter, Action... actions) {
        this.is = is;
        this.worldFilter = worldFilter;
        this.languageManager = LanguageManager.getInstance();
        this.actions = actions;

        this.plugin = MxLib.getPlugin();
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    /** Compares by type and display name (if present). */
    public boolean isItemTheSame(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) return false;
        if (item.getType() != is.getType()) return false;

        if (is.getItemMeta().hasDisplayName() && item.getItemMeta().hasDisplayName()) {
            return Functions.convertComponentToString(item.getItemMeta().displayName())
                    .equalsIgnoreCase(Functions.convertComponentToString(is.getItemMeta().displayName()));
        }
        return !is.getItemMeta().hasDisplayName();
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        if (Arrays.stream(actions).noneMatch(a -> a == e.getAction())) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack used = e.getItem();
        if (used == null || !used.hasItemMeta() || used.getType() == Material.AIR) return;

        Player p = e.getPlayer();

        if (!InventoryManager.validateItem(used, is)) return;

        // Generic, overridable policy hook

        T context = createContext();

        if (!canExecuteInteract(p, used, e, context)) {
            return;
        }

        try {
            execute(p, e, context);
        } catch (Exception ex) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXITEM,
                    "Could not execute item: " + Functions.convertComponentToString(is.getItemMeta().displayName()));
            ex.printStackTrace();
            MessageUtil.sendMessageToPlayer(p,
                    languageManager.getLanguageString(
                            StandardLanguageText.ERROR_WHILE_EXECUTING_ITEM,
                            Collections.emptyList(),
                            ChatPrefixManager.getInstance().requireFind(StandardChatPrefix.DEFAULT)));
        }
    }

    @EventHandler
    public void onBreakBlockWithItem(BlockBreakEvent e) {
        Player p = e.getPlayer();

        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (!inHand.hasItemMeta() || inHand.getType() == Material.AIR) return;

        if (!InventoryManager.validateItem(inHand, is)) return;

        // Generic, overridable policy hook
        if (!canExecuteBreak(p, inHand, e)) {
            e.setCancelled(true);
            return;
        }

        if (worldFilter != null) {
            if (!worldFilter.isPlayerInCorrectWorld(p)) {
                MessageUtil.sendMessageToPlayer(p,
                        languageManager.getLanguageString(
                                StandardLanguageText.NOT_CORRECT_WORLD,
                                Collections.emptyList(),
                                ChatPrefixManager.getInstance().requireFind(StandardChatPrefix.DEFAULT)));
                return;
            }
        }

        try {
            executeOnBreak(p, e);
        } catch (Exception ex) {
            Logger.logMessage(LogLevel.ERROR, StandardPrefix.MXITEM,
                    "Could not execute item: " + Functions.convertComponentToString(is.getItemMeta().displayName()));
            ex.printStackTrace();
            MessageUtil.sendMessageToPlayer(p,
                    languageManager.getLanguageString(
                            StandardLanguageText.ERROR_WHILE_EXECUTING_ITEM,
                            Collections.emptyList(),
                            ChatPrefixManager.getInstance().requireFind(StandardChatPrefix.DEFAULT)));
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

    public abstract void execute(Player p, PlayerInteractEvent e, T context);

    public abstract void executeOnBreak(Player p, BlockBreakEvent e);

    public abstract T createContext();
}
