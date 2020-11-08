package me.viscar.townyrelationalcolors;

import me.viscar.townyrelationalcolors.commands.Commands;
import me.viscar.townyrelationalcolors.commands.TabComplete;
import me.viscar.townyrelationalcolors.listeners.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Plugin that utilizes scoreboard teams to add in game relational coloring for other players depending
 * on their Towny relations
 */
public class TownyRelationalColors extends JavaPlugin {

    private Logger log = Bukkit.getLogger();
    private TownScoreboardManager townSbManager;

    ProtocolManager protocolManager;

    public void onEnable() {
        log.info("[RC] Booting up");
        saveDefaultConfig();

        townSbManager = new TownScoreboardManager();
        initListeners();

        protocolManager = ProtocolLibrary.getProtocolManager();
        if (getServer().getPluginManager().isPluginEnabled("AnimatedNames")) {
            protocolManager.addPacketListener(new PacketAdapter(this,
                    ListenerPriority.NORMAL,
                    PacketType.Play.Server.SCOREBOARD_TEAM) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_TEAM) {
                        PacketContainer packet = event.getPacket();
                        String packetName = packet.getStrings().read(0);
                        if (packetName != null && packetName.startsWith("RC"))
                            return;
                        if (packetName.startsWith("MVdWAN"))
                            event.setCancelled(true);
                    }
                }
            });
        }

        this.getCommand("rc").setExecutor(new Commands(townSbManager));
        this.getCommand("rc").setTabCompleter(new TabComplete());

        // For on reload
        for (Player player : Bukkit.getOnlinePlayers()) {
            townSbManager.onPlayerLogoff(player);
            townSbManager.onPlayerLogin(player);
        }
    }

    public void onDisable() {
        log.info("[RC] Shutting down ...");
    }


    /**
     * Registers all listeners
     */
    public void initListeners() {
        PluginManager plugin = this.getServer().getPluginManager();
        plugin.registerEvents(new PlayerLoginListener(townSbManager, this), this);
        plugin.registerEvents(new TownDisbandListener(townSbManager), this);
        plugin.registerEvents(new JoinLeaveTownListener(townSbManager), this);
        plugin.registerEvents(new JoinLeaveNationListener(townSbManager), this);
        plugin.registerEvents(new NationEnemyListener(townSbManager), this);
        plugin.registerEvents(new NationDisbandListener(townSbManager), this);
        plugin.registerEvents(new ScuffedNationAllyListener(this, townSbManager), this);
        plugin.registerEvents(new TownCreateListener(townSbManager, this), this);
    }

}
