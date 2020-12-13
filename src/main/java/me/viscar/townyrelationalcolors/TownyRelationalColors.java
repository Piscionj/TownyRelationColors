package me.viscar.townyrelationalcolors;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.viscar.townyrelationalcolors.commands.Commands;
import me.viscar.townyrelationalcolors.commands.TabComplete;
import me.viscar.townyrelationalcolors.listeners.*;
import me.viscar.townyrelationalcolors.townscoreboards.TownScoreboardManager;
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
    private static TownScoreboardManager townSbManager;

    ProtocolManager protocolManager;

    public void onEnable() {
        log.info("[RC] Booting up");
        saveDefaultConfig();

        townSbManager = new TownScoreboardManager();
        initListeners();

        // Hot fix for compatibility with AnimatedNames plugin (you're welcome Morby)
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

        this.getCommand("rc").setExecutor(new Commands());
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
        plugin.registerEvents(new PlayerLoginListener(), this);
        plugin.registerEvents(new TownDisbandListener(), this);
        plugin.registerEvents(new JoinLeaveTownListener(), this);
        plugin.registerEvents(new JoinLeaveNationListener(), this);
        plugin.registerEvents(new NationEnemyListener(), this);
        plugin.registerEvents(new NationDisbandListener(), this);
        plugin.registerEvents(new ScuffedNationAllyListener(), this);
        plugin.registerEvents(new TownCreateListener(), this);
    }

    /**
     * @return instance of the scoreboard manager
     */
    public static TownScoreboardManager getScoreboardManager() {
        return townSbManager;
    }

}
