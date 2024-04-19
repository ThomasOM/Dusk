package me.thomazz.dusk.check.impl;

import me.thomazz.dusk.check.Check;
import me.thomazz.dusk.check.event.flag.TimingFlagData;
import me.thomazz.dusk.ping.PingTask;
import me.thomazz.dusk.player.PlayerData;
import me.thomazz.dusk.util.Constants;

public class TimingCheck extends Check {
    private long clientTimePassed; // Amount of time has passed according to client
    private long pingTimePassed; // Amount of time has passed according to server synchronized with client

    public TimingCheck(PlayerData data) {
        super(data);
    }

    @Override
    public void onClientTick() {
        // Lower bound is the last synced timestamp and the maximum amount of ticks the client can lag for
        long maxCatchupTime = Constants.MAX_CATCHUP_TICKS * Constants.TICK_MILLIS;
        long lowerBound = Math.max(this.pingTimePassed - maxCatchupTime, 0L);

        // Upper bound is the current server time minus the time the player has logged in
        long upperBound = this.data.getPlugin().getCurrentServerTime() - this.data.getLoginTime();

        // Every tick increments the client time passed, but the time can not go below the lower bound
        this.clientTimePassed = Math.max(this.clientTimePassed + Constants.TICK_MILLIS, lowerBound);

        // If the client runs faster than our server time
        if (this.clientTimePassed > upperBound) {
            long timeOver = this.clientTimePassed - upperBound; // Time over the upper bound
            this.flag(new TimingFlagData(timeOver));
        }
    }

    @Override
    public void onPingSendEnd() {
        // Server time synchronization with client
        long time = System.currentTimeMillis();
        this.data.getPingTaskScheduler().scheduleTask(
            PingTask.end(() -> this.ping(time))
        );
    }

    public void ping(long time) {
        this.pingTimePassed = time - this.data.getLoginTime();
    }
}
