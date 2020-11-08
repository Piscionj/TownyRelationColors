package me.viscar.townyrelationalcolors.commands;

import me.viscar.townyrelationalcolors.TownScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private TownScoreboardManager tsbm;

    public Commands(TownScoreboardManager tsbm) {
        this.tsbm = tsbm;
    }

    /**
     * Command handler
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        // Check they are trying to do the reload command
        if (strings.length <= 0 || !strings[0].equalsIgnoreCase("reload"))
            return false;
        // Check they have proper permissions
        if (!commandSender.hasPermission("relationcolors.admin")) {
            commandSender.sendMessage(ChatColor.RED + "[RC] You do not have permission to use this command");
            return false;
        }
        commandSender.sendMessage(ChatColor.AQUA + "[RC] Reloading plugin ...");
        Bukkit.getPluginManager().getPlugin("TownyRelationalColors").reloadConfig();
        tsbm.reinitialize();
        for (Player player : Bukkit.getOnlinePlayers())
            tsbm.onPlayerLogin(player);

        commandSender.sendMessage(ChatColor.AQUA + "[RC] Reloading plugin complete");
        return true;
    }

}
