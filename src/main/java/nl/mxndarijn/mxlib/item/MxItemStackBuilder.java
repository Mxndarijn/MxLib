package nl.mxndarijn.mxlib.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nl.mxndarijn.mxlib.MxLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class MxItemStackBuilder<T extends MxItemStackBuilder<T>> {
    ItemStack itemStack;
    ItemMeta itemMeta;
    List<String> lores;

    MxItemStackBuilder(Material mat) {
        this(mat, 1);
    }

    MxItemStackBuilder(Material mat, int amount) {
        itemStack = new ItemStack(mat, amount);
        itemMeta = itemStack.getItemMeta();
        lores = new ArrayList<>();
    }

    public T setName(String name) {
        itemMeta.displayName(MiniMessage.miniMessage().deserialize("<!i>" + name));
        return (T) this;
    }

    public T addLore(String lore) {
        lores.add(lore);
        return (T) this;
    }

    public T addBlankLore() {
        lores.add(" ");
        return (T) this;
    }

    public T addItemFlag(ItemFlag... flag) {
        itemMeta.addItemFlags(flag);
        return (T) this;
    }

    public T addEnchantment(Enchantment enchantment, int level, boolean ignoreLevelRestrictions) {
        itemMeta.addEnchant(enchantment, level, ignoreLevelRestrictions);
        return (T) this;
    }

    public T setUnbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        return (T) this;
    }

    public T addCustomTagString(String tagName, String value) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(MxLib.getPlugin(), tagName), PersistentDataType.STRING, value);
        return (T) this;
    }

    public ItemStack build() {
        List<Component> componentList = new ArrayList<>();
        lores.forEach(l -> {
            componentList.add(MiniMessage.miniMessage().deserialize("<!i>" + l));
        });
        itemMeta.lore(componentList);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public T addCustomTagString(String tagName, int value) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(MxLib.getPlugin(), tagName), PersistentDataType.INTEGER, value);

        return (T) this;
    }

    public T addCustomTagString(String persistentDataTag, boolean b) {
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(MxLib.getPlugin(), persistentDataTag), PersistentDataType.STRING, String.valueOf(b));

        return (T) this;
    }
}
