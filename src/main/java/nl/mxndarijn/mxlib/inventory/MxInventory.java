package nl.mxndarijn.mxlib.inventory;

import lombok.Getter;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

@Getter
public class MxInventory {
    private final Inventory inv;
    private final HashMap<Integer, MxItemClicked> onClickedMap;
    private final String name;
    private final boolean delete;
    private final boolean cancelEvent;
    private final boolean canBeClosed;

    private final MxOnInventoryCloseEvent closeEvent;

    public MxInventory(Inventory inv, String invName, HashMap<Integer, MxItemClicked> onClickedMap, boolean delete, boolean cancelEvent, boolean closed, MxOnInventoryCloseEvent closeEvent) {
        this.inv = inv;
        this.onClickedMap = onClickedMap;
        this.name = invName;
        this.delete = delete;
        this.cancelEvent = cancelEvent;
        this.canBeClosed = closed;
        this.closeEvent = closeEvent;
    }

}
