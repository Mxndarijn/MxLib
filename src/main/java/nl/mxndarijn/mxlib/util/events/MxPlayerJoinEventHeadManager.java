package nl.mxndarijn.mxlib.util.events;


import nl.mxndarijn.mxlib.MxLib;
import nl.mxndarijn.mxlib.inventory.heads.MxHeadManager;
import nl.mxndarijn.mxlib.inventory.heads.MxHeadSection;
import nl.mxndarijn.mxlib.inventory.heads.MxHeadsType;
import nl.mxndarijn.mxlib.logger.MxLogLevel;
import nl.mxndarijn.mxlib.logger.MxLogger;
import nl.mxndarijn.mxlib.logger.MxStandardPrefix;
import nl.mxndarijn.mxlib.util.MxBedrockUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Optional;
import java.util.UUID;

public class MxPlayerJoinEventHeadManager implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(MxLib.getPlugin(), () -> {
            UUID uuid = e.getPlayer().getUniqueId();
            String key = uuid.toString();
            Optional<MxHeadSection> section = MxHeadManager.getInstance().getHeadSection(key);
            if (section.isEmpty()) {
                if (MxBedrockUtil.isBedrockPlayer(uuid)) {
                    // Bedrock players don't have a valid Java skull item — fetch texture directly via Geyser API
                    MxHeadManager.getInstance().storeBedrockSkullTexture(uuid, e.getPlayer().getName());
                } else {
                    ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();

                    // Set the owner of the skull to the player
                    skullMeta.setOwningPlayer(e.getPlayer());

                    // Set the modified SkullMeta on the head item
                    headItem.setItemMeta(skullMeta);

                    MxHeadManager.getInstance().storeSkullTexture(headItem, key, e.getPlayer().getName(), MxHeadsType.PLAYER);
                }
                MxLogger.logMessage(MxLogLevel.DEBUG, MxStandardPrefix.MXHEAD_MANAGER, "Added skull of " + e.getPlayer().getName() + " (" + uuid + ")");
            }
        }, 1);
    }
}
