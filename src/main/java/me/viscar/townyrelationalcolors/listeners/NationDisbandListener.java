package me.viscar.townyrelationalcolors.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PreDeleteNationEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import me.viscar.townyrelationalcolors.TownyRelationalColors;
import me.viscar.townyrelationalcolors.townscoreboards.TownScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NationDisbandListener implements Listener {

    private TownScoreboardManager tsbm;
    public NationDisbandListener(){
        this.tsbm = TownyRelationalColors.getScoreboardManager();
    }

    /**
     * Listens for when a nation is deleted/disbanded
     */
    @EventHandler
    public void onNationDisband(PreDeleteNationEvent e){
        try {
            Nation nation = TownyAPI.getInstance().getDataSource().getNation(e.getNationName());
            tsbm.onNationDisband(nation);
        } catch (NotRegisteredException ex) {
            ex.printStackTrace();
        }
    }
}
