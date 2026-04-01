package nl.mxndarijn.mxlib.spawnprotection.spawn;

import net.kyori.adventure.text.Component;
import nl.mxndarijn.mxlib.mxeventbus.core.MxSubscribe;
import nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventContext;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldType;
import nl.mxndarijn.mxlib.mxeventbus.global.MxWorldTypes;
import nl.mxndarijn.mxlib.spawnprotection.events.base.MxGlobalEventListener;
import nl.mxndarijn.mxlib.spawnprotection.events.pipeline.events.MxGlobalChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles global spawn chat and private map chat routing via the
 * {@link nl.mxndarijn.mxlib.mxeventbus.global.MxGlobalEventBus}.
 *
 * <p>If the sender is in a map world with private map chat enabled,
 * the message is delivered only to players in that same map world using the
 * map-chat format. Otherwise the message is broadcast globally, skipping players
 * who are in a game or in a map world with private chat enabled.</p>
 *
 * <p>All WIDM-specific logic (language strings, map lookups, host preferences,
 * game-world checks) is delegated to the injected {@link MxISpawnChatProvider}.</p>
 */
public final class MxSpawnChatListener extends MxGlobalEventListener {

    private final MxISpawnChatProvider provider;

    /**
     * Constructs a new {@code MxSpawnChatListener}.
     *
     * @param provider the {@link MxISpawnChatProvider} supplying chat-routing dependencies
     */
    public MxSpawnChatListener(MxISpawnChatProvider provider) {
        this.provider = provider;
    }

    /**
     * Routes chat messages originating from the spawn world.
     * Cancels the underlying paper event and handles delivery manually.
     *
     * @param ctx the event context wrapping a {@link MxGlobalChatEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.SPAWN)
    public void routeSpawnChat(MxGlobalEventContext<MxGlobalChatEvent, MxWorldType> ctx) {
        MxGlobalChatEvent event = ctx.event();
        Player sender = event.getPlayer();
        if (provider.isPlayerInGameWithSpectatorCheck(sender.getUniqueId())) return;
        event.getPaperEvent().setCancelled(true);
        broadcastGlobalChat(provider.buildGlobalChatMessage(sender, event.getPaperEvent().message()));
    }

    /**
     * Routes chat messages originating from a preset world.
     * Cancels the underlying paper event and broadcasts globally.
     *
     * @param ctx the event context wrapping a {@link MxGlobalChatEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.PRESET)
    public void routePresetChat(MxGlobalEventContext<MxGlobalChatEvent, MxWorldType> ctx) {
        MxGlobalChatEvent event = ctx.event();
        Player sender = event.getPlayer();
        if (provider.isPlayerInGameWithSpectatorCheck(sender.getUniqueId())) return;
        event.getPaperEvent().setCancelled(true);
        broadcastGlobalChat(provider.buildGlobalChatMessage(sender, event.getPaperEvent().message()));
    }

    /**
     * Routes chat messages originating from a map world.
     * When private map chat is enabled for the sender,
     * the message is scoped to the map world only; otherwise it is broadcast globally.
     *
     * @param ctx the event context wrapping a {@link MxGlobalChatEvent}
     */
    @MxSubscribe
    @MxWorldTypes(MxWorldType.MAP)
    public void routeMapChat(MxGlobalEventContext<MxGlobalChatEvent, MxWorldType> ctx) {
        MxGlobalChatEvent event = ctx.event();
        Player sender = event.getPlayer();
        if (provider.isPlayerInGameWithSpectatorCheck(sender.getUniqueId())) return;
        event.getPaperEvent().setCancelled(true);
        Optional<UUID> senderMapWorldUid = provider.getMapWorldUid(sender.getWorld());
        boolean privateMapChat = provider.hasPrivateMapChat(sender.getUniqueId());
        if (senderMapWorldUid.isPresent() && privateMapChat) {
            broadcastPrivateMapChat(sender, senderMapWorldUid.get(), event.getPaperEvent().message());
            return;
        }
        broadcastGlobalChat(provider.buildGlobalChatMessage(sender, event.getPaperEvent().message()));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Sends a private map-scoped chat message to all players in the sender's world.
     *
     * @param sender       the player who sent the message
     * @param mapWorldUid  the UUID of the map world to scope the message to
     * @param message      the raw chat message component
     */
    private void broadcastPrivateMapChat(Player sender, UUID mapWorldUid, Component message) {
        Component mapChatComponent = provider.buildMapChatMessage(sender, message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().getUID().equals(mapWorldUid)) {
                p.sendMessage(mapChatComponent);
            }
        }
    }

    /**
     * Broadcasts a global chat message to all eligible players and the console.
     * Players who are currently in a game are skipped. Players who are in a map world
     * with private chat enabled are also skipped.
     *
     * @param finalMessage the formatted message component to send
     */
    private void broadcastGlobalChat(Component finalMessage) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (provider.isPlayerInGameWithSpectatorCheck(p.getUniqueId())) continue;
            if (provider.shouldExcludeFromGlobalBroadcast(p)) continue;
            p.sendMessage(finalMessage);
        }
        Bukkit.getConsoleSender().sendMessage(finalMessage);
    }
}

