package me.thomazz.dusk.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Event called when player attacks another player.
 * Provides range the player attacked with using {@link ReachEvent#getRange()}
 * Range can be absent if no intercept was found tracing the ray.
 */
@Getter
public class ReachEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    @Nullable private final Double range; // Null when no interception of ray

    public ReachEvent(Player player, @Nullable Double range) {
        super(true);
        this.player = player;
        this.range = range;
    }

    public Optional<Double> getRange() {
        return Optional.ofNullable(this.range);
    }

    public static HandlerList getHandlerList() {
        return ReachEvent.HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return ReachEvent.HANDLER_LIST;
    }
}
