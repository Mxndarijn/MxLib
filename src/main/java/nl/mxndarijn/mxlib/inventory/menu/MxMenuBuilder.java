package nl.mxndarijn.mxlib.inventory.menu;

import lombok.Getter;
import nl.mxndarijn.mxlib.inventory.MxInventory;
import nl.mxndarijn.mxlib.inventory.MxInventoryBuilder;
import nl.mxndarijn.mxlib.inventory.MxInventoryManager;
import nl.mxndarijn.mxlib.inventory.MxInventorySlots;
import nl.mxndarijn.mxlib.item.MxDefaultItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Builder for menus that support a "previous" navigation entry.
 * Avoids storing Optional fields; plain nullable references are used instead.
 */
@Getter
public class MxMenuBuilder<T extends MxInventoryBuilder<T>> extends MxInventoryBuilder<T> {

    /** Menu to open when clicking the "previous" item (nullable). */
    protected MxInventory previousMenu;

    /** Item representing the "previous" action. */
    protected ItemStack previousItem;

    /** Slot index for the "previous" item. */
    protected int previousItemStackSlot;

    public MxMenuBuilder(String name, MxInventorySlots slotType) {
        super(name, slotType);
        this.previousMenu = null;
        this.previousItem = MxDefaultItemStackBuilder.create(Material.BARRIER, 1)
                .setName("<gray>Back")
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS)
                .build();
        this.previousItemStackSlot = slotType.slots - 9;
    }

    /** Sets the menu to navigate back to. */
    @SuppressWarnings("unchecked")
    public T setPrevious(MxInventory menu) {
        this.previousMenu = menu;
        return (T) this;
    }

    /** Sets the item used for the "previous" action. */
    @SuppressWarnings("unchecked")
    public T setPreviousItemStack(ItemStack previousItem) {
        this.previousItem = previousItem;
        return (T) this;
    }

    /** Sets the slot index where the "previous" item is placed. */
    @SuppressWarnings("unchecked")
    public T setPreviousItemStackSlot(int slot) {
        this.previousItemStackSlot = slot;
        return (T) this;
    }

    public Optional<MxInventory> getPreviousMenu() { return Optional.ofNullable(previousMenu); }

    @Override
    public MxInventory build() {
        if (previousMenu != null && previousItem != null) {
            setItem(previousItem, previousItemStackSlot, (inv, e) -> {
                if (e instanceof InventoryClickEvent ice && ice.getWhoClicked() instanceof Player player) {
                    MxInventoryManager.getInstance().addAndOpenInventory(player, previousMenu);
                }
            });
        }
        return super.build();
    }
}
