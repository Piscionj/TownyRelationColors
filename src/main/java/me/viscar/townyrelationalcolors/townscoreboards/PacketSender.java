package me.viscar.townyrelationalcolors.townscoreboards;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class PacketSender {

    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    /**
     * Sends team packets to the given player with the given team info
     */
    public static void sendTeamPackets(Player sendTo, ChatColor color, String prefix, Collection<String> playersToSend) {
        PacketContainer redundantPacket = createRedundantTeamPacket(color, prefix, playersToSend);
        PacketContainer teamPacket = createTeamPacket(color, prefix, playersToSend);

        try {
            protocolManager.sendServerPacket(sendTo, redundantPacket);
            protocolManager.sendServerPacket(sendTo, teamPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static PacketContainer createRedundantTeamPacket(ChatColor color, String prefix, Collection<String> players) {
        PacketContainer teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        // Set mode
        teamPacket.getIntegers().write(0, 0);
        // Set name
        teamPacket.getStrings().write(0, "RC" + color + prefix);
        // Set prefix
        teamPacket.getChatComponents().write(1, WrappedChatComponent.fromText(prefix));
        // Set players to send
        teamPacket.getSpecificModifier(Collection.class).write(0, players);
        // Set colors
        teamPacket.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);

        return teamPacket;
    }

    public static PacketContainer createTeamPacket(ChatColor color, String prefix, Collection<String> players) {
        PacketContainer redundantPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        // Set mode
        redundantPacket.getIntegers().write(0, 3);
        // Set name
        redundantPacket.getStrings().write(0, "RC" + color + prefix);
        // Set prefix
        redundantPacket.getChatComponents().write(1, WrappedChatComponent.fromText(prefix));
        // Set players to send
        redundantPacket.getSpecificModifier(Collection.class).write(0, players);
        // Set colors
        redundantPacket.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);

        return redundantPacket;
    }
}
