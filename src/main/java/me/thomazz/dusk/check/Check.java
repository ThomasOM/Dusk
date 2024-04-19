package me.thomazz.dusk.check;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.RequiredArgsConstructor;
import me.thomazz.dusk.check.event.CheckFlagEvent;
import me.thomazz.dusk.player.PlayerData;
import org.bukkit.Bukkit;

@CheckInfo
@RequiredArgsConstructor
public abstract class Check {
    protected final PlayerData data;

    public void onPacketReceive(PacketReceiveEvent event) {
    }

    public void onPacketSend(PacketSendEvent event) {
    }

    public void onClientTick() {
    }

    public void onPingSendStart() {
    }

    public void onPingSendEnd() {
    }

    public void onPongReceiveStart() {
    }

    public void onPongReceiveEnd() {
    }

    protected <T> void flag(T data) {
        Class<? extends Check> type = this.getClass();
        CheckInfo info = CheckRegistry.getInfo(type);
        CheckFlagEvent event = new CheckFlagEvent(this.data.getPlayer(), type, info, data);
        this.data.getPlugin().callEvent(event);
    }
}
