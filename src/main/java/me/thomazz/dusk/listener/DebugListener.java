package me.thomazz.dusk.listener;

import me.thomazz.dusk.check.event.CheckFlagEvent;
import me.thomazz.dusk.check.event.flag.ReachFlagData;
import me.thomazz.dusk.check.event.flag.TimingFlagData;
import me.thomazz.dusk.check.impl.ReachCheck;
import me.thomazz.dusk.check.impl.TimingCheck;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;

public class DebugListener implements Listener {
    @EventHandler
    public void onCheckFlag(CheckFlagEvent event) {
        if (event.getType() == ReachCheck.class) {
            ReachFlagData data = event.getData();
            String reachString = new DecimalFormat("0.000").format( data.getRange());
            Bukkit.broadcastMessage(ChatColor.RED + event.getPlayer().getName() + " attack reach: " + reachString);
        }

        if (event.getType() == TimingCheck.class) {
            TimingFlagData data = event.getData();
            Bukkit.broadcastMessage(ChatColor.RED + event.getPlayer().getName() + " timing over: " + data.getTimeOver());
        }
    }
}
