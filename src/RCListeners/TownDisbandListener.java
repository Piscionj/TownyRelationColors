package RCListeners;

import RCMain.TownScoreboardManager;
import com.palmergames.bukkit.towny.event.PreDeleteTownEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownDisbandListener implements Listener {

    private TownScoreboardManager tsbm;
    public TownDisbandListener(TownScoreboardManager tsbm){
        this.tsbm = tsbm;
    }

    /**
     * Listens for when a town is deleted/disbanded
     */
    @EventHandler
    public void onDisband(PreDeleteTownEvent e){
        Town town = e.getTown();
        for(Resident res : town.getResidents()) {
            Player player = Bukkit.getPlayer(res.getName());
            if(player == null)
                continue;
            if(player.isOnline())
                tsbm.playerLeaveTown(player, e.getTown());
        }
        tsbm.removeTown(e.getTown());
    }
}
