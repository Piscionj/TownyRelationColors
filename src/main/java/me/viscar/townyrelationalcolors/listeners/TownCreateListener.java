package me.viscar.townyrelationalcolors.listeners;

import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.object.Town;
import me.viscar.townyrelationalcolors.TownyRelationalColors;
import me.viscar.townyrelationalcolors.townscoreboards.TownScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownCreateListener implements Listener {

    private TownScoreboardManager tsbm;
    public TownCreateListener(){
        this.tsbm = TownyRelationalColors.getScoreboardManager();
    }

    /**
     * Listens for the creation of a new town
     */
    @EventHandler
    public void onTownCreate(NewTownEvent e){
        Town town = e.getTown();
        tsbm.addTown(town);
        Player player = Bukkit.getPlayer(e.getTown().getMayor().getName());
        tsbm.getTeamsContainer(town).addPlayer(player);
        tsbm.getTeamsContainer(town).sendPacketsToPlayer(player);
    }



}
