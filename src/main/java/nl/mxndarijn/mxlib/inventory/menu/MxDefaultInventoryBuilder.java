package nl.mxndarijn.mxlib.inventory.menu;


import nl.mxndarijn.mxlib.inventory.MxInventoryBuilder;
import nl.mxndarijn.mxlib.inventory.MxInventorySlots;

/**
 * Default implementation of {@link MxInventoryBuilder}.
 */
public class MxDefaultInventoryBuilder extends MxInventoryBuilder<MxDefaultInventoryBuilder> {
    /**
     * Constructs a new {@code MxDefaultInventoryBuilder}.
     * @param name the inventory name
     * @param slotType the slot type
     */
    protected MxDefaultInventoryBuilder(String name, MxInventorySlots slotType) {
        super(name, slotType);
    }

    /**
     * Creates a new instance of {@code MxDefaultInventoryBuilder}.
     * @param name the inventory name
     * @param slotType the slot type
     * @return the builder instance
     */
    public static MxDefaultInventoryBuilder create(String name, MxInventorySlots slotType) {
        return new MxDefaultInventoryBuilder(name, slotType);
    }
}
