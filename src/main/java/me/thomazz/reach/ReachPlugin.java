package me.thomazz.reach;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import dev.thomazz.pledge.Pledge;
import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.ClientPingerListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.thomazz.reach.event.ReachEvent;
import me.thomazz.reach.ping.PingTaskScheduler;
import me.thomazz.reach.player.PlayerData;
import me.thomazz.reach.util.Constants;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ReachPlugin extends JavaPlugin implements PacketListener, ClientPingerListener, Listener {
    private final Map<Player, PlayerData> playerDataMap = new HashMap<>();
    private Pledge pledge;

    @Override
    public void onLoad() {
        // Set up packet events
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Set up pledge
        this.pledge = Pledge.getOrCreate(this);
        ClientPinger pinger = this.pledge.createPinger(-1, -400); // Range doesn't matter here
        pinger.attach(this);

        // Register packet listeners
        PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.HIGHEST);
        PacketEvents.getAPI().init();

        // Register as event listener
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onReach(ReachEvent event) {
        Optional<Double> range = event.getRange();
        String reachString = range.map(new DecimalFormat("0.000")::format).orElse("No intercept");
        boolean reaching = !range.isPresent() || range.get() > Constants.MAX_RANGE;

        // Just broadcasting range for testing
        ChatColor color = reaching ? ChatColor.RED : ChatColor.YELLOW;
        Bukkit.broadcastMessage(color + event.getPlayer().getName() + " attack reach: " + reachString);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.playerDataMap.put(player, new PlayerData(this, player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerDataMap.remove(event.getPlayer());
    }

    @Override
    public void onPingSendStart(Player player, int id) {
        this.getPlayerData(player)
            .map(PlayerData::getPingTaskScheduler)
            .ifPresent(PingTaskScheduler::onPingSendStart);
    }

    @Override
    public void onPingSendEnd(Player player, int id) {
        this.getPlayerData(player)
            .map(PlayerData::getPingTaskScheduler)
            .ifPresent(PingTaskScheduler::onPingSendEnd);
    }

    @Override
    public void onPongReceiveStart(Player player, int id) {
        this.getPlayerData(player)
            .map(PlayerData::getPingTaskScheduler)
            .ifPresent(PingTaskScheduler::onPongReceiveStart);
    }

    @Override
    public void onPongReceiveEnd(Player player, int id) {
        this.getPlayerData(player)
            .map(PlayerData::getPingTaskScheduler)
            .ifPresent(PingTaskScheduler::onPongReceiveEnd);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        this.getPlayerData(player).ifPresent(data -> data.handlePacketReceive(event));
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Player player = (Player) event.getPlayer();
        this.getPlayerData(player).ifPresent(data -> data.handlePacketSend(event));
    }

    public Optional<PlayerData> getPlayerData(Player player) {
        return Optional.ofNullable(this.playerDataMap.get(player));
    }
}
