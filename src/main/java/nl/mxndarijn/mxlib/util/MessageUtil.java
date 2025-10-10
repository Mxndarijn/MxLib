package nl.mxndarijn.mxlib.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    public static void sendMessageToPlayer(CommandSender player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<!i>" + message));
    }

}
