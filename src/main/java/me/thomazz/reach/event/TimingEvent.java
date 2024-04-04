package me.thomazz.reach.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class TimingEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final long timeOver;

    public TimingEvent(Player player, long timeOver) {
        super(true);
        this.player = player;
        this.timeOver = timeOver;
    }

    public static HandlerList getHandlerList() {
        return TimingEvent.HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return TimingEvent.HANDLER_LIST;
    }
}
