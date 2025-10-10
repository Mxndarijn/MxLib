package nl.mxndarijn.mxlib.item;

import org.bukkit.Material;

public class MxDefaultItemStackBuilder extends MxItemStackBuilder<MxDefaultItemStackBuilder> {

    MxDefaultItemStackBuilder(Material mat) {
        super(mat);
    }

    MxDefaultItemStackBuilder(Material mat, int amount) {
        super(mat, amount);
    }

    public static MxDefaultItemStackBuilder create(Material mat) {
        return new MxDefaultItemStackBuilder(mat);
    }

    public static MxDefaultItemStackBuilder create(Material mat, int amount) {
        return new MxDefaultItemStackBuilder(mat, amount);
    }
}
