package nl.mxndarijn.mxlib.item;

import org.bukkit.Material;

/**
 * Default implementation of {@link MxItemStackBuilder}.
 */
public class MxDefaultItemStackBuilder extends MxItemStackBuilder<MxDefaultItemStackBuilder> {

    /**
     * Constructs a new {@code MxDefaultItemStackBuilder}.
     * @param mat the material
     */
    MxDefaultItemStackBuilder(Material mat) {
        super(mat);
    }

    /**
     * Constructs a new {@code MxDefaultItemStackBuilder}.
     * @param mat the material
     * @param amount the stack size
     */
    MxDefaultItemStackBuilder(Material mat, int amount) {
        super(mat, amount);
    }

    /**
     * Creates a new instance of {@code MxDefaultItemStackBuilder}.
     * @param mat the material
     * @return the builder instance
     */
    public static MxDefaultItemStackBuilder create(Material mat) {
        return new MxDefaultItemStackBuilder(mat);
    }

    /**
     * Creates a new instance of {@code MxDefaultItemStackBuilder}.
     * @param mat the material
     * @param amount the stack size
     * @return the builder instance
     */
    public static MxDefaultItemStackBuilder create(Material mat, int amount) {
        return new MxDefaultItemStackBuilder(mat, amount);
    }
}
