package me.viscar.townyrelationalcolors.listeners;

import me.viscar.townyrelationalcolors.TownyRelationalColors;
import me.viscar.townyrelationalcolors.townscoreboards.TownScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerLoginListener implements Listener {
    private TownScoreboardManager tsbm;
    public PlayerLoginListener() {
        this.tsbm = TownyRelationalColors.getScoreboardManager();
    }

    /**
     * Listens for player login to apply their town's relation scoreboard
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        tsbm.onPlayerLogin(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLogoff(PlayerQuitEvent e){
        tsbm.onPlayerLogoff(e.getPlayer());
    }
}
