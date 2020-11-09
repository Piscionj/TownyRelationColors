package me.viscar.townyrelationalcolors.listeners;

import me.viscar.townyrelationalcolors.TownScoreboardManager;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JoinLeaveNationListener implements Listener {

    TownScoreboardManager tsbm;

    public JoinLeaveNationListener(TownScoreboardManager tsbm){
        this.tsbm = tsbm;
    }

    /**
     * Listens for towns joining nations
     */
    @EventHandler
    public void onJoinNation(NationAddTownEvent e){
        Town town = e.getTown();
        Nation nation = e.getNation();
        if(e.getNation().getCapital() == null) //True when nation first created - creating a new nation counts as the capital joining the new nation
            return;
        tsbm.townJoinNation(town, nation);
    }


    /**
     * Listens for towns leaving nations
     */
    @EventHandler
    public void onLeaveNation(NationRemoveTownEvent e) {
        Town town = e.getTown();
        Nation nation = e.getNation();
        tsbm.townLeaveNation(town, nation);
    }
}
