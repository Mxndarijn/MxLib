package nl.mxndarijn.mxlib.inventory;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MxInventoryManager implements Listener {

    private static MxInventoryManager instance;
    private final HashMap<UUID, List<MxInventory>> inventories;
    private final JavaPlugin plugin;
    private MxInventoryManager(JavaPlugin plugin) {
        inventories = new HashMap<>();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    public static void init(JavaPlugin plugin) {
        if( instance != null) {
            throw new IllegalStateException("MxInventoryManager is already initialized!");
        }
        instance = new MxInventoryManager(plugin);
    }

    public static MxInventoryManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MxInventoryManager is initialized!");
        }
        return instance;
    }

    @EventHandler
    public void InventoryClick(InventoryClickEvent e) {
        e.getWhoClicked();
        if (e.getClickedInventory() == null) {
            return;
        } else {
            e.getView().title();
        }

        UUID uuid = e.getWhoClicked().getUniqueId();
        if (!inventories.containsKey(uuid)) {
            return;
        }
        List<MxInventory> get = inventories.get(uuid);
        for (int i = 0; i < get.size(); i++) {
            MxInventory mxInventory = get.get(i);
            if (e.getClickedInventory() == mxInventory.getInv()) {
                if (mxInventory.isCancelEvent()) {
                    e.setCancelled(true);
                }
                MxItemClicked clicked = mxInventory.getOnClickedMap().getOrDefault(e.getSlot(), null);
                if (clicked != null) {
                    clicked.OnItemClicked(mxInventory, e);
                }
                break;
            }
        }
    }

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
            String title = e.getView().getTitle();
            if (e.getView().title() instanceof TextComponent) {
                title = ((TextComponent) e.getView().title()).content();
            }
            if (mxInventory.getName().contains(title)) {
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
                        list.remove(mxInventory);
                        break;
                    }
                }
            }
        }
    }

    public void addInventory(UUID uuid, MxInventory inv) {
        if (inventories.containsKey(uuid)) {
            inventories.get(uuid).add(inv);
        } else {
            inventories.put(uuid, new ArrayList<>(Collections.singletonList(inv)));
        }
    }

    public void addAndOpenInventory(Player p, MxInventory inv) {
        if (p == null) {
            return;
        }
        addInventory(p.getUniqueId(), inv);
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            p.openInventory(inv.getInv());
        });

    }

    public void addAndOpenInventory(UUID uuid, MxInventory inv) {
        Player p = Bukkit.getPlayer(uuid);
        addAndOpenInventory(p, inv);
    }
}
