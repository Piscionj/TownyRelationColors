package me.viscar.townyrelationalcolors.townscoreboards;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


public class TownTeamsContainer {

    protected Town town;
    protected TownScoreboardManager tsbm;
    protected ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    protected Set<Player> rankedTownMembers = new LinkedHashSet<>();
    protected LinkedHashSet<String> nonRankedMembers = new LinkedHashSet<>();

    public TownTeamsContainer(TownScoreboardManager tsbm, Town town) {
        this.tsbm = tsbm;
        this.town = town;
    }

    public void addPlayer(Player player) {
        if (tsbm.hasCustomRanks(player))
            rankedTownMembers.add(player);
        else
            nonRankedMembers.add(player.getName());
    }

    public void removePlayer(Player player) {
        if (tsbm.hasCustomRanks(player))
            rankedTownMembers.remove(player);
        else
            nonRankedMembers.remove(player.getName());
    }

    public void sendPacketsFromPlayer(Player player) {
        Resident res;
        ChatColor relColor;
        try {
            res = TownyAPI.getInstance().getDataSource().getResident(player.getName());
            relColor = getRelation(res);
        } catch (NotRegisteredException e) {
            Bukkit.getLogger().info("[RC] Towny exception while sending packets to " + player.getName() + ", aborting");
            return;
        }
        if (relColor.equals(ChatColor.WHITE))
            relColor = tsbm.getRankColor(player);
        String prefix = tsbm.getPlayerPrefixes(player);

        PacketContainer redundantPacket = PacketSender.createRedundantTeamPacket(relColor, prefix, Arrays.asList(player.getName()));
        PacketContainer teamPacket = PacketSender.createTeamPacket(relColor, prefix, Arrays.asList(player.getName()));

        try {
            // Send packets for ranked members
            for (Player townMember : rankedTownMembers) {
                protocolManager.sendServerPacket(townMember, redundantPacket);
                protocolManager.sendServerPacket(townMember, teamPacket);
            }
            // Send packets for non ranked members
            for (String playerName : nonRankedMembers) {
                Player townMember = Bukkit.getPlayer(playerName);
                if (townMember == null)
                    continue;
                protocolManager.sendServerPacket(townMember, redundantPacket);
                protocolManager.sendServerPacket(townMember, teamPacket);
            }
        } catch (InvocationTargetException e) {
            Bukkit.getLogger().info("[RC] Runtime error while sending packets to town " + town.getName() + ", aborting");
            return;
        }
    }

    public void sendPacketsFromNation(Nation nation) {
        for (Player nationMember : TownyAPI.getInstance().getOnlinePlayers(nation))
            sendPacketsFromPlayer(nationMember);
    }

    public void sendPacketsToNation(Nation nation) {
        for (Player nationMember : TownyAPI.getInstance().getOnlinePlayers(nation))
            sendPacketsToPlayer(nationMember);
    }

    public void sendPacketsToPlayer(Player player) {
        try {
            Resident res = TownyAPI.getInstance().getDataSource().getResident(player.getName());
            ChatColor relColor = getRelation(res);
            // Send packets for non ranked members
            if (!nonRankedMembers.isEmpty())
                PacketSender.sendTeamPackets(player, relColor, "", nonRankedMembers);

            // Send packets for ranked members
            if (!rankedTownMembers.isEmpty()) {
                for (Player townMember : rankedTownMembers) {
                    if (relColor.equals(ChatColor.WHITE))
                        relColor = tsbm.getRankColor(townMember);
                    String prefixes = tsbm.getPlayerPrefixes(townMember);
                    PacketSender.sendTeamPackets(player, relColor, prefixes, Arrays.asList(townMember.getName()));
                }
            }
        } catch (NotRegisteredException e) {
            Bukkit.getLogger().info("[RC] Towny exception while sending packets to " + player.getName() + ", aborting");
            return;
        }

    }


    public ChatColor getRelation(Resident res) throws NotRegisteredException {
        // If they don't have a town
        if (!res.hasTown())
            return ChatColor.WHITE;
        Town resTown = res.getTown();
        // If they are in the same town
        if (resTown.equals(this.town))
            return tsbm.relationStringToColor("town");
        // If either has no nation
        if (!resTown.hasNation() || !this.town.hasNation())
            return ChatColor.WHITE;
        Nation resNat = resTown.getNation();
        Nation townNation = this.town.getNation();
        // If they are in the same nation
        if (resNat.equals(townNation))
            return tsbm.relationStringToColor("nation");

        // If they are allies
        if (resNat.hasAlly(townNation))
            return tsbm.relationStringToColor("ally");

        // If they are enemies
        if (resNat.hasEnemy(townNation) || townNation.hasEnemy(resNat))
            return tsbm.relationStringToColor("enemy");

        // Default
        return ChatColor.WHITE;
    }

}
