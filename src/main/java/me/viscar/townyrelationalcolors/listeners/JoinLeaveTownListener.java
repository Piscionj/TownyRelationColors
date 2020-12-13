package me.viscar.townyrelationalcolors.listeners;

import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import me.viscar.townyrelationalcolors.TownyRelationalColors;
import me.viscar.townyrelationalcolors.townscoreboards.TownScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JoinLeaveTownListener implements Listener {
    private TownScoreboardManager tsbm;

    public JoinLeaveTownListener(){
        this.tsbm = TownyRelationalColors.getScoreboardManager();
    }

    /**
     * Listens for players joining a town
     */
    @EventHandler
    public void onJoinTown(TownAddResidentEvent e) {
        if(e.getTown().getMayor() == null || e.getResident().equals(e.getTown().getMayor()))
            return;
        Player player = Bukkit.getPlayer(e.getResident().getName());
        tsbm.playerJoinTown(player, e.getTown());
    }

    /**
     * Listens for players leaving a town
     */
    @EventHandler
    public void onLeaveTown(TownRemoveResidentEvent e){
        String playerName = e.getResident().getName();
        Player player = Bukkit.getPlayer(playerName);
        if(player == null || !player.isOnline())
            return;
        tsbm.playerLeaveTown(player, e.getTown());
    }

}
