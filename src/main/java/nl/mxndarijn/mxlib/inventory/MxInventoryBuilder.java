package nl.mxndarijn.mxlib.inventory;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class MxInventoryBuilder<T extends MxInventoryBuilder<T>> {
    protected Inventory inv;
    protected HashMap<Integer, MxItemClicked> onClickedMap;
    protected MxInventorySlots slotType;
    protected String name;
    protected boolean delete = true;
    protected boolean cancelEvent = true;
    protected boolean canBeClosed = true;

    protected MxOnInventoryCloseEvent closeEvent = null;

    protected MxInventoryBuilder(String name, MxInventorySlots slotType) {
        this.slotType = slotType;
        this.name = name;
        inv = Bukkit.createInventory(null, slotType.slots, MiniMessage.miniMessage().deserialize("<!i>" + this.name));
        onClickedMap = new HashMap<>();
    }

    /*public static MxInventoryBuilder create(String name, MxInventorySlots slotType) {
        return new MxInventoryBuilder(name, slotType);
    }*/

    public T addItem(ItemStack is, MxItemClicked onClicked) {
        int index = inv.firstEmpty();
        if (index == -1) {
            return (T) this;
        }
        inv.setItem(index, is);
        onClickedMap.put(index, onClicked);
        return (T) this;
    }

    public T setItem(ItemStack is, int slot, MxItemClicked onClicked) {
        inv.setItem(slot, is);
        onClickedMap.put(slot, onClicked);
        return (T) this;
    }

    public T setOnInventoryCloseEvent(MxOnInventoryCloseEvent closeEvent) {
        this.closeEvent = closeEvent;
        return (T) this;
    }

    public T deleteInventoryWhenClosed(boolean delete) {
        this.delete = delete;
        return (T) this;
    }

    public T canBeClosed(boolean closed) {
        this.canBeClosed = closed;
        return (T) this;
    }


    public MxInventory build() {
        return new MxInventory(inv, name, onClickedMap, delete, cancelEvent, canBeClosed, closeEvent);
    }

    public T defaultCancelEvent(boolean b) {
        cancelEvent = b;
        return (T) this;
    }

    public T changeTitle(String newTitle) {
        this.name = newTitle;
        Inventory inventory = Bukkit.createInventory(null, slotType.slots, MiniMessage.miniMessage().deserialize("<!i>" + this.name));
        onClickedMap.forEach((index, clicked) -> {
            inventory.setItem(index, inv.getItem(index));
        });
        this.inv = inventory;
        return (T) this;
    }


    private int getRandom(int[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
}
