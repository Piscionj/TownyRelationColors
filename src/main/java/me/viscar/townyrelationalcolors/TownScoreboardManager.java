package me.viscar.townyrelationalcolors;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Manages and keeps track of each town's relation scoreboards
 */
public class TownScoreboardManager {

    //Constructor
    public TownScoreboardManager() {
        initConfigFields();
        initTeamsContainers();
    }

    public void reinitialize() {
        initConfigFields();
        initTeamsContainers();
    }

    private void initTeamsContainers() {
        townTeams.clear();
        for (Town town : TownyAPI.getInstance().getDataSource().getTowns()) {
            townTeams.put(town, new TownTeamsContainer(this, town));
        }
    }

    public TownTeamsContainer getTeamsContainer(Town town) {
        return townTeams.get(town);
    }

    public void addTown(Town town) {
        townTeams.put(town, new TownTeamsContainer(this, town));
    }

    public void removeTown(Town town) {
        townTeams.remove(town);
    }

    public List<Town> getEffectedTowns(Town town) {
        List<Town> effectedTowns = new ArrayList<>();
        effectedTowns.add(town);
        if (!town.hasNation())
            return effectedTowns;
        Nation nation = null;
        try {
            nation = town.getNation();
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
        effectedTowns.addAll(nation.getTowns());
        // Add enemy/ally towns
        for (Nation nations : TownyAPI.getInstance().getDataSource().getNations()) {
            if (nation.hasEnemy(nations) || nations.hasEnemy(nation) || nation.hasAlly(nations))
                effectedTowns.addAll(nations.getTowns());
        }
        return effectedTowns;
    }

    public List<Nation> getEffectedNations(Nation nation) {
        List<Nation> effectedNations = new ArrayList<>();
        effectedNations.add(nation);
        for (Nation possiblyEffected : TownyAPI.getInstance().getDataSource().getNations()) {
            if (nation.hasAlly(possiblyEffected) || nation.hasEnemy(possiblyEffected) || possiblyEffected.hasEnemy(nation))
                effectedNations.add(possiblyEffected);
        }
        return effectedNations;
    }

    /**
     * Sets the player's scoreboard to their town's on login and handles the player's toggle
     */
    public void onPlayerLogin(Player player) {
        HashSet<String> addedGroups = new HashSet<>();
        ArrayList<Rank> playerRanksList = new ArrayList<>();
        for (Rank rank : customRanks) {
            String rankGroup = rank.getGroup();
            if (rank.isRank(player) && !addedGroups.contains(rankGroup)) {
                addedGroups.add(rankGroup);
                playerRanksList.add(rank);
            }
        }
        playerRanks.put(player, playerRanksList);

        try {
            Resident res = TownyAPI.getInstance().getDataSource().getResident(player.getName());
            if (res.hasTown())
                getTeamsContainer(res.getTown()).addPlayer(player);
            else
                wilderness.addPlayer(player);
        } catch (NotRegisteredException e) {
            wilderness.addPlayer(player);
        }
        for (TownTeamsContainer teamsContainer : townTeams.values()) {
            teamsContainer.sendPacketsFromPlayer(player);
            teamsContainer.sendPacketsToPlayer(player);
        }
        wilderness.sendPacketsFromPlayer(player);
        wilderness.sendPacketsToPlayer(player);
    }

    /**
     * Removes player from tracking on logoff
     */
    public void onPlayerLogoff(Player player) {
        try {
            Resident resident = TownyAPI.getInstance().getDataSource().getResident(player.getName());
            if (resident.hasTown())
                getTeamsContainer(resident.getTown()).removePlayer(player);
            else
                wilderness.removePlayer(player);
        } catch (NotRegisteredException e) {
            wilderness.removePlayer(player);
        }
    }

    // Town join/leave nation

    /**
     * Handles relation updates for when a town leaves a nation
     */
    public void townLeaveNation(Town town, Nation nation) {
        townJoinLeaveHelper(town, nation);
    }

    /**
     * Handles relation updates for when a town joins a nation
     */
    public void townJoinNation(Town town, Nation nation) {
        townJoinLeaveHelper(town, nation);
    }

    /**
     * Helper function for a town joining/leaving a nation
     */
    private void townJoinLeaveHelper(Town town, Nation nation) {
        TownTeamsContainer teamsContainer = getTeamsContainer(town);
        for (Nation effectedNation : getEffectedNations(nation)) {
            teamsContainer.sendPacketsToNation(effectedNation);
            teamsContainer.sendPacketsFromNation(effectedNation);
        }
    }

    /**
     * Handles relation updates for when a nation disbands
     */
    public void onNationDisband(Nation nation) {
        List<Nation> effectedNations = getEffectedNations(nation);
        effectedNations.remove(nation);
        List<Town> nationTowns = new ArrayList<>(nation.getTowns());
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Nation nat : effectedNations) {
                    for (Town town : nationTowns) {
                        TownTeamsContainer teamsContainer = getTeamsContainer(town);
                        teamsContainer.sendPacketsFromNation(nat);
                        teamsContainer.sendPacketsToNation(nat);
                    }
                }
            }
        }.runTaskLaterAsynchronously(plugin, 1);
    }

    // Player leaving/joining town

    /**
     * Handles relation updates for when a player joins a town
     */
    public void playerJoinTown(Player player, Town town) {
        getTeamsContainer(town).addPlayer(player);
        wilderness.removePlayer(player);
        playerJoinLeaveTownHelper(player, town);
    }

    /**
     * Handles relation updates for when a player leaves a town
     */
    public void playerLeaveTown(Player player, Town town) {
        TownTeamsContainer teamsContainer = getTeamsContainer(town);
        teamsContainer.removePlayer(player);
        wilderness.addPlayer(player);
        playerJoinLeaveTownHelper(player, town);

        // Player leave town method acts after the player leaves, so remove green from self separately
        String prefix = getPlayerPrefixes(player);
        ChatColor relColor = getRankColor(player);
        PacketContainer redundantPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        PacketContainer teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        // Set mode
        redundantPacket.getIntegers().write(0, 0);
        teamPacket.getIntegers().write(0, 3);
        // Set name
        redundantPacket.getStrings().write(0, "RC" + relColor + prefix);
        teamPacket.getStrings().write(0, "RC" + relColor + prefix);
        // Set prefix
        teamPacket.getChatComponents().write(1, WrappedChatComponent.fromText(prefix));
        // Set players to send
        teamPacket.getSpecificModifier(Collection.class).write(0, Arrays.asList(player.getName()));

        try {
            protocolManager.sendServerPacket(player, teamPacket);
            protocolManager.sendServerPacket(player, redundantPacket);
        } catch (InvocationTargetException e) {
            Bukkit.getLogger().info("[RC] Runtime error while sending packets to " + player.getName() + " on leaving town, aborting");
            return;
        }
    }

    /**
     * Helper function for when a player joins/leaves a town
     */
    private void playerJoinLeaveTownHelper(Player player, Town town) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Town effectedTown : getEffectedTowns(town)) {
                    TownTeamsContainer teamsContainer = getTeamsContainer(effectedTown);
                    teamsContainer.sendPacketsToPlayer(player);
                    teamsContainer.sendPacketsFromPlayer(player);
                }
            }
        }.runTaskLaterAsynchronously(plugin, 1);

    }

    // Enemy handling

    public void onNationUnEnemy(Nation nation, Nation neutral) {
        nationRelationHelper(nation, neutral);
    }

    public void onNationEnemy(Nation nation, Nation enemy) {
        nationRelationHelper(nation, enemy);
    }

    // Ally handling

    public void onNationAlly(Nation nation, Nation ally) {
        nationRelationHelper(nation, ally);
    }

    public void onNationUnAlly(Nation nation, Nation neutral) {
        nationRelationHelper(nation, neutral);
    }

    /**
     * Helper function for when a nation changes relations with another nation
     */
    private void nationRelationHelper(Nation nationOne, Nation nationTwo) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Town natOneTowns : nationOne.getTowns()) {
                    getTeamsContainer(natOneTowns).sendPacketsToNation(nationTwo);
                    getTeamsContainer(natOneTowns).sendPacketsFromNation(nationTwo);
                }
            }
        }.runTaskLaterAsynchronously(plugin, 1);
    }

    public ChatColor relationStringToColor(String rel) {
        rel = rel.toLowerCase();
        switch (rel) {
            case "town":
                return townColor;
            case "nation":
                return nationColor;
            case "enemy":
                return enemyColor;
            case "ally":
                return allyColor;
            default:
                return ChatColor.WHITE;
        }
    }

    public String getPlayerPrefixes(Player player) {
        StringBuilder teams = new StringBuilder();
        for (Rank rank : playerRanks.get(player))
            teams.append(rank.getPrefix());
        String prefixes = teams.toString();
        return prefixes.isEmpty() ? prefixes : prefixes + spaceBetween;
    }

    public ChatColor getRankColor(Player player) {
        ArrayList<Rank> playerRankList = playerRanks.get(player);
        if (playerRankList.isEmpty())
            return ChatColor.WHITE;
        return playerRankList.get(0).getColor();
    }

    public boolean hasCustomRanks(Player player) {
        return !playerRanks.get(player).isEmpty();
    }

    // FIELDS

    private void initConfigFields() {
        this.plugin = Bukkit.getPluginManager().getPlugin("TownyRelationalColors");

        townColor = ChatColor.valueOf(plugin.getConfig().getString("relationColor.town"));
        nationColor = ChatColor.valueOf(plugin.getConfig().getString("relationColor.nation"));
        enemyColor = ChatColor.valueOf(plugin.getConfig().getString("relationColor.enemy"));
        allyColor = ChatColor.valueOf(plugin.getConfig().getString("relationColor.ally"));

        if (plugin.getConfig().getBoolean("spaceBetweenPrefixAndName"))
            spaceBetween = " ";

        // Read in and parse custom ranks from config
        for (String rankLine : plugin.getConfig().getStringList("customRanks")) {
            String[] rankParts = rankLine.split(",");
            ChatColor color = ChatColor.valueOf(rankParts[0]);
            if (color == null) {
                Bukkit.getLogger().severe("[RC] Error reading in custom rank color: Color " + rankParts[0] + " does not exist. Defaulting to white.");
                color = ChatColor.WHITE;
            }
            String prefix = rankParts[1];
            if (prefix.equalsIgnoreCase("NULL"))
                prefix = "";
            String group = rankParts[2];
            ArrayList<String> rankPermissions = new ArrayList<>();
            for (int i = 3; i < rankParts.length; i++)
                rankPermissions.add(rankParts[i]);
            customRanks.add(new Rank(color, prefix, group, rankPermissions));
        }

        wilderness = new WildernessTeamsContainer(this);
    }

    private ChatColor townColor;
    private ChatColor nationColor;
    private ChatColor enemyColor;
    private ChatColor allyColor;

    private String spaceBetween = "";

    private ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private WildernessTeamsContainer wilderness;
    private HashMap<Town, TownTeamsContainer> townTeams = new HashMap<>();
    private HashMap<Player, ArrayList<Rank>> playerRanks = new HashMap<>();
    private ArrayList<Rank> customRanks = new ArrayList<>();
    private Plugin plugin;
}
