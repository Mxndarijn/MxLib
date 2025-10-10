package nl.mxndarijn.mxlib.changeworld;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public interface WorldReachedZeroPlayersEvent {
    void worldReachedZeroPlayers(Player p, World w, PlayerChangedWorldEvent e);
}
