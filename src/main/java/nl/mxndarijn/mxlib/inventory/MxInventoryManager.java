package nl.mxndarijn.mxlib.inventory;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Manager for {@link MxInventory} instances.
 * Tracks open inventories for players and handles inventory events.
 */
public class MxInventoryManager implements Listener {

    private static MxInventoryManager instance;
    private final HashMap<UUID, List<MxInventory>> inventories;
    private final JavaPlugin plugin;
    private MxInventoryManager(JavaPlugin plugin) {
        inventories = new HashMap<>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    /**
     * Initializes the singleton instance.
     * @param plugin the {@link JavaPlugin} instance
     */
    public static void init(JavaPlugin plugin) {
        if( instance != null) {
            throw new IllegalStateException("MxInventoryManager is already initialized!");
        }
        instance = new MxInventoryManager(plugin);
    }

    /**
     * Gets the singleton instance.
     * @return the {@code MxInventoryManager} instance
     * @throws IllegalStateException if not initialized
     */
    public static MxInventoryManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MxInventoryManager is not initialized!");
        }
        return instance;
    }

    /**
     * Handles inventory clicks and dispatches them to the corresponding {@link MxInventory}.
     * @param e the inventory click event
     */
    @EventHandler
    public void InventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }

        UUID uuid = e.getWhoClicked().getUniqueId();
        if (!inventories.containsKey(uuid)) {
            return;
        }

        List<MxInventory> playerInventories = inventories.get(uuid);
        for (MxInventory mxInventory : playerInventories) {
            // Check if the clicked inventory is the MxInventory
            if (e.getClickedInventory().equals(mxInventory.getInv())) {
                if (mxInventory.isCancelEvent()) {
                    e.setCancelled(true);
                }
                MxItemClicked clicked = mxInventory.getOnClickedMap().getOrDefault(e.getSlot(), null);
                if (clicked != null) {
                    clicked.OnItemClicked(mxInventory, e);
                }
                return;
            }

            // Check if the player clicked their own inventory while an MxInventory is open as the top inventory
            if (e.getView().getTopInventory().equals(mxInventory.getInv())) {
                if (e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                    if (!mxInventory.isAllowBottomInventoryInteraction()) {
                        e.setCancelled(true);
                    }
                }
                return;
            }
        }
    }

    /**
     * Handles inventory closing.
     * @param e the inventory close event
     */
    @EventHandler
    void InventoryClose(InventoryCloseEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!inventories.containsKey(uuid)) {
            return;
        }
        List<MxInventory> list = inventories.get(uuid);
        Iterator<MxInventory> i = list.iterator();
        while (i.hasNext()) {
            MxInventory mxInventory = i.next();
            if (e.getInventory().equals(mxInventory.getInv())) {
                if (!mxInventory.isCanBeClosed()) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        e.getPlayer().openInventory(mxInventory.getInv());
                    }, 1);
                } else {
                    if (mxInventory.getCloseEvent() != null) {
                        Player p = (Player) e.getPlayer();
                        mxInventory.getCloseEvent().onClose(p, mxInventory, e);
                    }
                    if (mxInventory.isDelete()) {
                        i.remove();
                        if (list.isEmpty()) {
                            inventories.remove(uuid);
                        }
                    }
                }
                return;
            }
        }
    }

    /**
     * Registers an {@link MxInventory} for a player.
     * @param uuid the player's UUID
     * @param inv the inventory
     */
    public void addInventory(UUID uuid, MxInventory inv) {
        if (inventories.containsKey(uuid)) {
            inventories.get(uuid).add(inv);
        } else {
            inventories.put(uuid, new ArrayList<>(Collections.singletonList(inv)));
        }
    }

    /**
     * Adds an inventory for a player and opens it immediately.
     * Clears all previous tracked inventories for the player.
     * @param p the player
     * @param inv the inventory
     */
    public void addAndOpenInventory(Player p, MxInventory inv) {
        if (p == null) {
            return;
        }
        removeAllInventories(p.getUniqueId());
        addInventory(p.getUniqueId(), inv);
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            p.openInventory(inv.getInv());
        });

    }

    /**
     * Adds an inventory for a player and opens it immediately.
     * @param uuid the player's UUID
     * @param inv the inventory
     */
    public void addAndOpenInventory(UUID uuid, MxInventory inv) {
        Player p = Bukkit.getPlayer(uuid);
        addAndOpenInventory(p, inv);
    }

    /**
     * Clears tracked inventories when a player joins.
     * @param e the player join event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        removeAllInventories(e.getPlayer().getUniqueId());
    }

    /**
     * Clears tracked inventories when a player quits.
     * @param e the player quit event
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removeAllInventories(e.getPlayer().getUniqueId());
    }

    /**
     * Removes a specific inventory for a player.
     * @param uuid the player's UUID
     * @param inv the inventory
     */
    public void removeInventory(UUID uuid, MxInventory inv) {
        if (inventories.containsKey(uuid)) {
            List<MxInventory> list = inventories.get(uuid);
            list.remove(inv);
            if (list.isEmpty()) {
                inventories.remove(uuid);
            }
        }
    }

    /**
     * Clears all tracked inventories for a player.
     * @param uuid the player's UUID
     */
    public void removeAllInventories(UUID uuid) {
        inventories.remove(uuid);
    }
}
