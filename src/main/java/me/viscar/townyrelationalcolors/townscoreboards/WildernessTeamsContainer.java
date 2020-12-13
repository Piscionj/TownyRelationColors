package me.viscar.townyrelationalcolors.townscoreboards;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class WildernessTeamsContainer extends TownTeamsContainer {

    public WildernessTeamsContainer(TownScoreboardManager tsbm) {
        super(tsbm, null);
    }


    public void sendPacketsFromPlayer(Player player) {
        ChatColor relColor = tsbm.getRankColor(player);
        String prefix = tsbm.getPlayerPrefixes(player);
        PacketContainer redundantPacket = PacketSender.createRedundantTeamPacket(relColor, prefix, Arrays.asList(player.getName()));
        PacketContainer teamPacket = PacketSender.createTeamPacket(relColor, prefix, Arrays.asList(player.getName()));

        try {
            // Send packets for ranked members
            for (Player wildPlayer : rankedTownMembers) {
                protocolManager.sendServerPacket(wildPlayer, redundantPacket);
                protocolManager.sendServerPacket(wildPlayer, teamPacket);
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
            Bukkit.getLogger().info("[RC] Runtime error while sending packets to townless players, aborting");
            return;
        }
    }

    public void sendPacketsToPlayer(Player player) {
            // Send packets for non ranked members
            if (!nonRankedMembers.isEmpty())
                PacketSender.sendTeamPackets(player, ChatColor.WHITE, "", nonRankedMembers);
            // Send packets for ranked members
            if (!rankedTownMembers.isEmpty()) {
                for (Player townMember : rankedTownMembers) {
                    ChatColor rankColor = tsbm.getRankColor(townMember);
                    String prefixes = tsbm.getPlayerPrefixes(townMember);
                    PacketSender.sendTeamPackets(player, rankColor, prefixes, Arrays.asList(townMember.getName()));
                }
            }

    }

}
