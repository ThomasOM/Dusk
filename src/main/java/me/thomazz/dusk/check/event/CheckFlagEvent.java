package me.thomazz.dusk.check.event;

import lombok.Getter;
import me.thomazz.dusk.check.Check;
import me.thomazz.dusk.check.CheckInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class CheckFlagEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Class<? extends Check> type;
    private final CheckInfo info;
    private final Object data;

    public CheckFlagEvent(Player player, Class<? extends Check> type, CheckInfo info, Object data) {
        super(true);
        this.player = player;
        this.type = type;
        this.info = info;
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) this.data;
    }

    public static HandlerList getHandlerList() {
        return CheckFlagEvent.HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return CheckFlagEvent.HANDLER_LIST;
    }
}
