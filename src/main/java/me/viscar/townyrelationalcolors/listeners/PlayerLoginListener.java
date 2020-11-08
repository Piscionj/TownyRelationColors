package me.viscar.townyrelationalcolors.listeners;

import me.viscar.townyrelationalcolors.TownScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;


public class PlayerLoginListener implements Listener {
    private TownScoreboardManager tsbm;
    private Plugin plugin;

    public PlayerLoginListener(TownScoreboardManager tsbm, Plugin plugin) {
        this.tsbm = tsbm;
        this.plugin = plugin;
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
