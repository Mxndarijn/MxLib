package nl.mxndarijn.mxlib.inventory.menu;


import nl.mxndarijn.mxlib.inventory.MxInventorySlots;

public class MxDefaultMenuBuilder extends MxMenuBuilder<MxDefaultMenuBuilder> {

    public MxDefaultMenuBuilder(String name, MxInventorySlots slotType) {
        super(name, slotType);
    }

    public static MxDefaultMenuBuilder create(String name, MxInventorySlots slotType) {
        return new MxDefaultMenuBuilder(name, slotType);
    }
}
