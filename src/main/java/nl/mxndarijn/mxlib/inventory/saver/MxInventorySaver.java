package nl.mxndarijn.mxlib.inventory.saver;

import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.util.MxFunctions;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for saving and loading player inventories to/from YAML config files,
 * and for checking item presence or validity within inventories.
 */
public class MxInventorySaver {

    /**
     * Saves all items in the given inventory to the specified config path and writes the file.
     *
     * @param file the file to save to
     * @param fc   the file configuration to write into
     * @param path the config path prefix under which items are stored
     * @param inv  the inventory to save
     */
    public static void saveInventory(File file, FileConfiguration fc, String path, Inventory inv) {
        fc.set(path, null);
        for (int i = 0; i < inv.getSize(); i++) {
            fc.set(path + "." + i, inv.getItem(i));
        }
        try {
            fc.save(file);
        } catch (IOException e) {
            MxLogger.logMessage(MxLogLevel.ERROR, "Could not save inventory to: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Loads inventory items from the config into the player's inventory.
     * Slots not present in the config are set to air.
     *
     * @param fc   the file configuration to read from
     * @param path the config path prefix under which items are stored
     * @param p    the player whose inventory will be populated
     */
    public static void loadInventoryForPlayer(FileConfiguration fc, String path, Player p) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack is = new ItemStack(Material.AIR);
            if (fc.contains(path + "." + i)) {
                ItemStack newIs = fc.getItemStack(path + "." + i);
                if (newIs != null && newIs.getType() != Material.AIR) {
                    is = newIs;
                }
            }
            p.getInventory().setItem(i, is);
        }
    }

    /**
     * Checks whether the inventory contains an item with the same type and display name as the given item.
     *
     * @param inv the inventory to search
     * @param is  the item to match against
     * @return {@code true} if a matching item is found
     */
    public static boolean containsItem(Inventory inv, ItemStack is) {
        for (ItemStack itemStack : inv) {
            if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                if (itemStack.getType() == is.getType() && MxFunctions.convertComponentToString(itemStack.getItemMeta().displayName()).equals(MxFunctions.convertComponentToString(is.getItemMeta().displayName()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validates whether the given item stack matches the reference item by type and display name.
     *
     * @param itemStack the item stack to validate
     * @param is        the reference item to compare against
     * @return {@code true} if the item stack matches the reference
     */
    public static boolean validateItem(ItemStack itemStack, ItemStack is) {
        if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getType() == is.getType() && MxFunctions.convertComponentToString(itemStack.getItemMeta().displayName()).equals(MxFunctions.convertComponentToString(is.getItemMeta().displayName()));
        }

        return false;
    }
}
