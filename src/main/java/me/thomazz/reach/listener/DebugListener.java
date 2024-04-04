package me.thomazz.reach.listener;

import me.thomazz.reach.event.ReachEvent;
import me.thomazz.reach.event.TimingEvent;
import me.thomazz.reach.util.Constants;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.Optional;

public class DebugListener implements Listener {
    @EventHandler
    public void onReach(ReachEvent event) {
        Optional<Double> range = event.getRange();
        String reachString = range.map(new DecimalFormat("0.000")::format).orElse("No intercept");
        boolean reaching = !range.isPresent() || range.get() > Constants.MAX_RANGE;

        ChatColor color = reaching ? ChatColor.RED : ChatColor.YELLOW;
        Bukkit.broadcastMessage(color + event.getPlayer().getName() + " attack reach: " + reachString);
    }

    @EventHandler
    public void onTiming(TimingEvent event) {
        Bukkit.broadcastMessage(ChatColor.RED + event.getPlayer().getName() + " timing over: " + event.getTimeOver());
    }
}
