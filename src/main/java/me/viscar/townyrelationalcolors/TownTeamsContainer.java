package me.viscar.townyrelationalcolors;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
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
import java.util.Collection;
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
        // Set colors
        teamPacket.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, relColor);

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
            if (!nonRankedMembers.isEmpty()) {
                protocolManager.sendServerPacket(player, createRedundantPacket(relColor, ""));
                protocolManager.sendServerPacket(player, createPacket(relColor, "", nonRankedMembers));
            }
            // Send packets for ranked members
            if (!rankedTownMembers.isEmpty()) {
                for (Player townMember : rankedTownMembers) {
                    if (relColor.equals(ChatColor.WHITE))
                        relColor = tsbm.getRankColor(townMember);
                    String prefixes = tsbm.getPlayerPrefixes(townMember);
                    protocolManager.sendServerPacket(player, createRedundantPacket(relColor, prefixes));
                    protocolManager.sendServerPacket(player, createPacket(relColor, prefixes, Arrays.asList(player.getName())));
                }
            }
        } catch (InvocationTargetException e) {
            Bukkit.getLogger().info("[RC] Runtime error while sending packets for town " + town.getName() + ", aborting");
            return;
        } catch (NotRegisteredException e) {
            Bukkit.getLogger().info("[RC] Towny exception while sending packets to " + player.getName() + ", aborting");
            return;
        }

    }

    public PacketContainer createRedundantPacket(ChatColor color, String prefix) {
        PacketContainer teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        // Set mode
        teamPacket.getIntegers().write(0, 0);
        // Set name
        teamPacket.getStrings().write(0, "RC" + color + prefix);

        return teamPacket;
    }

    public PacketContainer createPacket(ChatColor color, String prefix, Collection<String> players) {
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
