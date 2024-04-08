package me.thomazz.dusk.timing;

import lombok.RequiredArgsConstructor;
import me.thomazz.dusk.DuskPlugin;
import me.thomazz.dusk.event.TimingEvent;
import me.thomazz.dusk.util.Constants;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Estimates player client time by synchronizing timestamps between server and client.
 * We assume that the server time follows the correct speed and the client should never be able to run faster.
 */
@RequiredArgsConstructor
public class Timing implements Listener {
    private final DuskPlugin plugin;
    private final Player player;
    private final long loginTime;

    private long clientTimePassed; // Amount of time has passed according to client
    private long pingTimePassed; // Amount of time has passed according to server synchronized with client

    public void tick() {
        // Lower bound is the last synced timestamp and the maximum amount of ticks the client can lag for
        long maxCatchupTime = Constants.MAX_CATCHUP_TICKS * Constants.TICK_MILLIS;
        long lowerBound = Math.max(this.pingTimePassed - maxCatchupTime, 0L);

        // Upper bound is the current server time minus the time the player has logged in
        long upperBound = this.plugin.getCurrentServerTime() - this.loginTime;

        // Every tick increments the client time passed, but the time can not go below the lower bound
        this.clientTimePassed = Math.max(this.clientTimePassed + Constants.TICK_MILLIS, lowerBound);

        // If the client runs faster than our server time
        if (this.clientTimePassed > upperBound) {
            long timeOver = this.clientTimePassed - upperBound; // Time over the upper bound
            this.plugin.callEvent(new TimingEvent(this.player, timeOver));
        }
    }

    // Server time synchronization with client
    public void ping(long time) {
        this.pingTimePassed = time - this.loginTime;
    }
}
