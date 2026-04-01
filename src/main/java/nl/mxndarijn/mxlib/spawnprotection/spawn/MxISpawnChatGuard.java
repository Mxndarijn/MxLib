package nl.mxndarijn.mxlib.spawnprotection.spawn;

import java.util.UUID;

/**
 * Guards the chat bridge in {@link MxSpawnBukkitBridge} against forwarding messages
 * from players who are already handled by the game event pipeline.
 *
 * <p>Implement this interface with WIDM-specific logic and inject it into
 * {@link MxSpawnBukkitBridge} so that the bridge itself contains no WIDM references.</p>
 */
@FunctionalInterface
public interface MxISpawnChatGuard {

    /**
     * Returns whether the player with the given UUID is currently in a game
     * (including as a spectator) and should therefore be excluded from the
     * global chat pipeline.
     *
     * @param uuid the player UUID to check; must not be {@code null}
     * @return {@code true} if the player is in a game or spectating one
     */
    boolean isPlayerInGame(UUID uuid);
}

