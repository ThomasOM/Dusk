package me.thomazz.dusk;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import dev.thomazz.pledge.Pledge;
import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.ClientPingerListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.thomazz.dusk.check.CheckRegistry;
import me.thomazz.dusk.listener.DebugListener;
import me.thomazz.dusk.player.PlayerData;
import me.thomazz.dusk.util.PluginLoggerFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class DuskPlugin extends JavaPlugin implements PacketListener, ClientPingerListener, Listener {
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("duskDebug"));

    private final Map<Player, PlayerData> playerDataMap = new LinkedHashMap<>();
    private Pledge pledge;

    @Override
    public void onLoad() {
        // Set up packet events
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false);
        PacketEvents.getAPI().load();

        // Set up logger utility
        PluginLoggerFactory.init(this);
    }

    @Override
    public void onEnable() {
        CheckRegistry.init();

        // Set up pledge
        this.pledge = Pledge.getOrCreate(this);
        ClientPinger pinger = this.pledge.createPinger(-1, -400); // Range doesn't matter here
        pinger.attach(this);

        // Register packet listeners
        PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.HIGHEST);
        PacketEvents.getAPI().init();

        // Register listeners
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new DebugListener(), this);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        this.playerDataMap.put(player, new PlayerData(this, player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.getPlayerData(event.getPlayer()).orElseThrow(IllegalStateException::new).join();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerDataMap.remove(event.getPlayer());
    }

    @Override
    public void onPingSendStart(Player player, int id) {
        this.getPlayerData(player).ifPresent(PlayerData::onPingSendStart);
    }

    @Override
    public void onPingSendEnd(Player player, int id) {
        this.getPlayerData(player).ifPresent(PlayerData::onPingSendEnd);
    }

    @Override
    public void onPongReceiveStart(Player player, int id) {
        this.getPlayerData(player).ifPresent(PlayerData::onPongReceiveStart);
    }

    @Override
    public void onPongReceiveEnd(Player player, int id) {
        this.getPlayerData(player).ifPresent(PlayerData::onPongReceiveEnd);
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

    public void callEvent(Event event) {
        this.getServer().getPluginManager().callEvent(event);
    }

    public long getCurrentServerTime() {
        return System.currentTimeMillis(); // Same as current system time
    }
}