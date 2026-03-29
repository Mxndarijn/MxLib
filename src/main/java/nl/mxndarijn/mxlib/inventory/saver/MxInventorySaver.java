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

public class MxInventorySaver {

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

    public static boolean validateItem(ItemStack itemStack, ItemStack is) {
        if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getType() == is.getType() && MxFunctions.convertComponentToString(itemStack.getItemMeta().displayName()).equals(MxFunctions.convertComponentToString(is.getItemMeta().displayName()));
        }

        return false;
    }
}
