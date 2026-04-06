package nl.mxndarijn.mxlib.inventory;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

/**
 * Fluent builder for creating {@link MxInventory} instances.
 *
 * @param <T> the concrete builder type for method chaining
 */
public class MxInventoryBuilder<T extends MxInventoryBuilder<T>> {
    protected Inventory inv;
    protected HashMap<Integer, MxItemClicked> onClickedMap;
    protected MxInventorySlots slotType;
    protected String name;
    protected boolean delete = true;
    protected boolean cancelEvent = true;
    protected boolean canBeClosed = true;
    protected boolean allowBottomInventoryInteraction = false;

    protected MxOnInventoryCloseEvent closeEvent = null;

    /**
     * Constructs a new builder with the given inventory name and slot layout.
     *
     * @param name     the display name of the inventory
     * @param slotType the slot layout type
     */
    protected MxInventoryBuilder(String name, MxInventorySlots slotType) {
        this.slotType = slotType;
        this.name = name;
        inv = Bukkit.createInventory(null, slotType.slots, MiniMessage.miniMessage().deserialize("<!i>" + this.name));
        onClickedMap = new HashMap<>();
    }

    /*public static MxInventoryBuilder create(String name, MxInventorySlots slotType) {
        return new MxInventoryBuilder(name, slotType);
    }*/

    /**
     * Adds an item to the first empty slot with the given click handler.
     *
     * @param is        the item to add
     * @param onClicked the click handler for this item
     * @return this builder
     */
    public T addItem(ItemStack is, MxItemClicked onClicked) {
        int index = inv.firstEmpty();
        if (index == -1) {
            return (T) this;
        }
        inv.setItem(index, is);
        onClickedMap.put(index, onClicked);
        return (T) this;
    }

    /**
     * Places an item at the specified slot with the given click handler.
     *
     * @param is        the item to place
     * @param slot      the slot index
     * @param onClicked the click handler for this item
     * @return this builder
     */
    public T setItem(ItemStack is, int slot, MxItemClicked onClicked) {
        inv.setItem(slot, is);
        onClickedMap.put(slot, onClicked);
        return (T) this;
    }

    /**
     * Sets the callback to invoke when the inventory is closed.
     *
     * @param closeEvent the close event callback
     * @return this builder
     */
    public T setOnInventoryCloseEvent(MxOnInventoryCloseEvent closeEvent) {
        this.closeEvent = closeEvent;
        return (T) this;
    }

    /**
     * Sets whether the inventory should be deleted when closed.
     *
     * @param delete {@code true} to delete on close
     * @return this builder
     */
    public T deleteInventoryWhenClosed(boolean delete) {
        this.delete = delete;
        return (T) this;
    }

    /**
     * Sets whether the player is allowed to close this inventory.
     *
     * @param closed {@code true} if the inventory can be closed
     * @return this builder
     */
    public T canBeClosed(boolean closed) {
        this.canBeClosed = closed;
        return (T) this;
    }


    /**
     * Sets whether interaction with the player's bottom inventory is allowed.
     *
     * @param allowBottomInventoryInteraction {@code true} to allow bottom inventory interaction
     * @return this builder
     */
    public T allowBottomInventoryInteraction(boolean allowBottomInventoryInteraction) {
        this.allowBottomInventoryInteraction = allowBottomInventoryInteraction;
        return (T) this;
    }

    /**
     * Builds and returns the configured {@link MxInventory}.
     *
     * @return the built inventory
     */
    public MxInventory build() {
        return new MxInventory(inv, name, onClickedMap, delete, cancelEvent, canBeClosed, closeEvent, allowBottomInventoryInteraction);
    }

    /**
     * Sets whether inventory click events should be cancelled by default.
     *
     * @param b {@code true} to cancel click events by default
     * @return this builder
     */
    public T defaultCancelEvent(boolean b) {
        cancelEvent = b;
        return (T) this;
    }

    /**
     * Changes the title of the inventory, recreating the underlying Bukkit inventory.
     *
     * @param newTitle the new title
     * @return this builder
     */
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
