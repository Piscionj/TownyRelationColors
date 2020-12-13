package me.viscar.townyrelationalcolors.listeners;

import com.palmergames.bukkit.towny.event.NationAddEnemyEvent;
import com.palmergames.bukkit.towny.event.NationRemoveEnemyEvent;
import com.palmergames.bukkit.towny.object.Nation;
import me.viscar.townyrelationalcolors.TownyRelationalColors;
import me.viscar.townyrelationalcolors.townscoreboards.TownScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NationEnemyListener implements Listener {

    private TownScoreboardManager tsbm;
    public NationEnemyListener(){
        this.tsbm = TownyRelationalColors.getScoreboardManager();
    }

    /**
     * Listens for when a nation enemies another nation
     */
    @EventHandler
    public void onEnemy(NationAddEnemyEvent e){
        Nation nat = e.getNation();
        Nation enemy = e.getEnemy();
        tsbm.onNationEnemy(nat, enemy);

    }

    /**
     * Listens for when a nation un-enemies another nation
     */
    @EventHandler
    public void unEnemy(NationRemoveEnemyEvent e){
        Nation nat = e.getNation();
        Nation enemy = e.getEnemy();
        if(!nat.hasEnemy(enemy) && !enemy.hasEnemy(nat))
            tsbm.onNationUnEnemy(nat, enemy);
    }



}
