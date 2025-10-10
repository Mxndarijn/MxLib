package nl.mxndarijn.mxlib.inventory.menu;


import nl.mxndarijn.mxlib.inventory.MxInventoryBuilder;
import nl.mxndarijn.mxlib.inventory.MxInventorySlots;

public class MxDefaultInventoryBuilder extends MxInventoryBuilder<MxDefaultInventoryBuilder> {
    protected MxDefaultInventoryBuilder(String name, MxInventorySlots slotType) {
        super(name, slotType);
    }

    public static MxDefaultInventoryBuilder create(String name, MxInventorySlots slotType) {
        return new MxDefaultInventoryBuilder(name, slotType);
    }
}
