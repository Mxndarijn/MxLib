package nl.mxndarijn.mxlib.inventory;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

/**
 * Represents a managed Bukkit inventory with click handlers, close events, and interaction policies.
 */
@Getter
public class MxInventory {
    private final Inventory inv;
    private final HashMap<Integer, MxItemClicked> onClickedMap;
    private final String name;
    private final boolean delete;
    private final boolean cancelEvent;
    @Setter
    private boolean canBeClosed;
    private final boolean allowBottomInventoryInteraction;

    private final MxOnInventoryCloseEvent closeEvent;

    /**
     * Constructs a new {@code MxInventory}.
     *
     * @param inv                            the underlying Bukkit inventory
     * @param invName                        the display name of the inventory
     * @param onClickedMap                   map of slot index to click handler
     * @param delete                         whether to delete the inventory when closed
     * @param cancelEvent                    whether to cancel inventory click events by default
     * @param closed                         whether the inventory can be closed by the player
     * @param closeEvent                     callback invoked when the inventory is closed
     * @param allowBottomInventoryInteraction whether to allow interaction with the player's bottom inventory
     */
    public MxInventory(Inventory inv, String invName, HashMap<Integer, MxItemClicked> onClickedMap, boolean delete, boolean cancelEvent, boolean closed, MxOnInventoryCloseEvent closeEvent, boolean allowBottomInventoryInteraction) {
        this.inv = inv;
        this.onClickedMap = onClickedMap;
        this.name = invName;
        this.delete = delete;
        this.cancelEvent = cancelEvent;
        this.canBeClosed = closed;
        this.closeEvent = closeEvent;
        this.allowBottomInventoryInteraction = allowBottomInventoryInteraction;
    }

}
