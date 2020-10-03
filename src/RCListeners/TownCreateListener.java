package RCListeners;

import RCMain.TownScoreboardManager;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class TownCreateListener implements Listener {

    private TownScoreboardManager tsbm;
    private Plugin plugin;

    public TownCreateListener(TownScoreboardManager tsbm, Plugin plugin){
        this.tsbm = tsbm;
        this.plugin = plugin;
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
