package RCMain;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Rank {

    private ChatColor color;
    private String prefix;
    private String group;
    private ArrayList<String> permissions;

    public Rank(ChatColor color, String prefix, String group, ArrayList<String> permissions) {
        this.color = color;
        this.prefix = prefix;
        this.group = group;
        this.permissions = permissions;
    }

    public boolean isRank(Player player) {
        for (String permission : permissions)
            if (player.hasPermission(permission))
                return true;
        return false;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getGroup() {
        return this.group;
    }


}
