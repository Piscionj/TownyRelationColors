package me.viscar.townyrelationalcolors;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

public class WildernessTeamsContainer extends TownTeamsContainer {

    public WildernessTeamsContainer(TownScoreboardManager tsbm) {
        super(tsbm, null);
    }


    public void sendPacketsFromPlayer(Player player) {
        ChatColor relColor = tsbm.getRankColor(player);
        String prefix = tsbm.getPlayerPrefixes(player);
        PacketContainer teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        PacketContainer redundantPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
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
        // Set colors
        teamPacket.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, relColor);
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
        try {
            // Send packets for non ranked members
            if (!nonRankedMembers.isEmpty()) {
                protocolManager.sendServerPacket(player, createRedundantPacket(ChatColor.WHITE, ""));
                protocolManager.sendServerPacket(player, createPacket(ChatColor.WHITE, "", nonRankedMembers));
            }
            // Send packets for ranked members
            if (!rankedTownMembers.isEmpty()) {
                for (Player townMember : rankedTownMembers) {
                    ChatColor rankColor = tsbm.getRankColor(townMember);
                    String prefixes = tsbm.getPlayerPrefixes(townMember);
                    protocolManager.sendServerPacket(player, createRedundantPacket(rankColor, prefixes));
                    protocolManager.sendServerPacket(player, createPacket(rankColor, prefixes, Arrays.asList(player.getName())));
                }
            }
        } catch (InvocationTargetException e) {
            Bukkit.getLogger().info("[RC] Runtime error while sending packets for town " + town.getName() + ", aborting");
            return;
        }
    }

}
