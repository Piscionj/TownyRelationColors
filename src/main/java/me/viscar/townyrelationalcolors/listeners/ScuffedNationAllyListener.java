package me.viscar.townyrelationalcolors.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NewNationEvent;
import com.palmergames.bukkit.towny.object.Nation;
import me.viscar.townyrelationalcolors.TownyRelationalColors;
import me.viscar.townyrelationalcolors.townscoreboards.TownScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


/**
 * Since there is no event listeners for allying/un-allying, this class scans through every nation
 *  every so often and checks for changes in ally relations
 *
 *  Will be removed once/if any proper ally event listeners are added to TownyAPI
 */
public class ScuffedNationAllyListener implements Listener {

    private TownScoreboardManager tsbm;
    private Plugin plugin;
    private HashMap<Nation, List<Nation>> natAllies; // Hashmap of nation to allies to check against
    private Set<String> toDelete = new HashSet<String>();
    private Set<Nation> toAdd = new HashSet<Nation>();

    public ScuffedNationAllyListener(){
        this.natAllies = new HashMap<Nation, List<Nation>>();
        this.plugin = Bukkit.getPluginManager().getPlugin("TownyRelationalColors");
        this.tsbm = TownyRelationalColors.getScoreboardManager();
        initNatAllies();
        initScanner();
    }

    @EventHandler
    public void onNewNation(NewNationEvent event){
        toAdd.add(event.getNation());
    }

    @EventHandler
    public void onNationDelete(DeleteNationEvent event){
        toDelete.add(event.getNationName());

    }

    private void initNatAllies() {
        for(Nation nation : TownyAPI.getInstance().getDataSource().getNations()) {
            ArrayList<Nation> allies = new ArrayList<Nation>();
            allies.addAll(nation.getAllies());
            natAllies.put(nation, allies);
        }
    }

    public void initScanner(){
        new BukkitRunnable() {

            @Override
            public void run() {

                removeDeletedNations();
                addNewNations();

                for(Nation nation : TownyAPI.getInstance().getDataSource().getNations()){
                        updateAllies(nation);
                }
            }

            }.runTaskTimerAsynchronously(plugin, 40, 20);
    }

    public void addNewNations(){
        for(Nation nation : toAdd) {
            ArrayList<Nation> allies = new ArrayList<Nation>();
            allies.addAll(nation.getAllies());
            natAllies.put(nation, allies);
        }

        toAdd.clear();
    }

    public void removeDeletedNations(){
        if(toDelete.isEmpty())
            return;
        Set<Nation> toDelFinal = new HashSet<Nation>();
        for(String str : toDelete) {
            for (Nation nation : natAllies.keySet()) {
                if (nation.getName().equals(str))
                    toDelFinal.add(nation);
            }
        }
        for(Nation nation : toDelFinal){
            natAllies.remove(nation);
        }
        toDelete.clear();
    }

    public void updateAllies(Nation nation) {
        if(nation == null) // Safety check
            return;
        if(natAllies.get(nation) == null) // Safety check
            return;
        //Check for new allies
        for(Nation ally : nation.getAllies()){
            if(!natAllies.get(nation).contains(ally)) {
                tsbm.onNationAlly(nation, ally);
            }
        }
        //Check for removed allies
        for(Nation ally : natAllies.get(nation)) {
            if(!nation.getAllies().contains(ally)) {
                tsbm.onNationUnAlly(nation, ally);
            }
        }
        ArrayList<Nation> allies = new ArrayList<Nation>();
        allies.addAll(nation.getAllies());
        natAllies.put(nation, allies);
    }
}
