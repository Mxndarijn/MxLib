package nl.mxndarijn.mxlib.inventory.menu;


import nl.mxndarijn.mxlib.inventory.MxInventorySlots;

/**
 * Default implementation of {@link MxMenuBuilder}.
 */
public class MxDefaultMenuBuilder extends MxMenuBuilder<MxDefaultMenuBuilder> {

    /**
     * Constructs a new {@code MxDefaultMenuBuilder}.
     * @param name the menu name
     * @param slotType the slot type
     */
    public MxDefaultMenuBuilder(String name, MxInventorySlots slotType) {
        super(name, slotType);
    }

    /**
     * Creates a new instance of {@code MxDefaultMenuBuilder}.
     * @param name the menu name
     * @param slotType the slot type
     * @return the builder instance
     */
    public static MxDefaultMenuBuilder create(String name, MxInventorySlots slotType) {
        return new MxDefaultMenuBuilder(name, slotType);
    }
}
