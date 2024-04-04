package me.thomazz.reach.ping;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Objects;
import java.util.Queue;

/**
 * Utility that schedules tasks to be executed on a client response to server tick pings.
 */
@Getter
public class PingTaskScheduler {
    private final Queue<Queue<PingTask>> scheduledTasks = new ArrayDeque<>();

    private boolean started;
    private boolean receivingPongs;

    @Nullable private Queue<PingTask> schedulingTaskQueue;
    @Nullable private Queue<PingTask> runningTaskQueue;

    /**
     * Schedules a runnable to execute when the response for the pings of the current server tick is received from the client.
     * <p>
     * @param task - Runnable to schedule
     */
    public void scheduleTask(PingTask task) {
        // Joining happens in the middle of the tick loop, so any tasks scheduled on start need to run already
        if (!this.started) {
            this.scheduledTasks.add(this.schedulingTaskQueue = new ArrayDeque<>());
            task.onStart();
        }

        // The client can also respond to the start ping before the end ping is sent, meaning tasks should already start
        if (Objects.equals(this.runningTaskQueue, this.schedulingTaskQueue)) {
            task.onStart();
        }

        Objects.requireNonNull(this.schedulingTaskQueue).add(task);
    }

    // Extra utility method for scheduling a task to run on the first pong
    public void scheduleStartTask(Runnable runnable) {
        this.scheduleTask(new PingTask() {
            @Override
            public void onStart() {
                runnable.run();
            }
        });
    }

    // Extra utility method for scheduling a task to run on the second pong
    public void scheduleEndTask(Runnable runnable) {
        this.scheduleTask(new PingTask() {
            @Override
            public void onEnd() {
                runnable.run();
            }
        });
    }

    // Called on tick start server ping
    public void onPingSendStart() {
        this.scheduledTasks.add(this.schedulingTaskQueue = new ArrayDeque<>());
        this.started = true;
    }

    // Called on tick end server ping
    public void onPingSendEnd() {
        this.schedulingTaskQueue = null;
    }

    // Called when tick start server ping response received from client
    public void onPongReceiveStart() {
        this.receivingPongs = true;
        this.runningTaskQueue = this.scheduledTasks.poll();

        if (this.runningTaskQueue != null) {
            this.runningTaskQueue.forEach(PingTask::onStart);
        }
    }

    // Called when tick end server ping response received from client
    public void onPongReceiveEnd() {
        this.receivingPongs = false;

        if (this.runningTaskQueue != null) {
           while (!this.runningTaskQueue.isEmpty()) {
               this.runningTaskQueue.poll().onEnd();
           }
        }

        this.runningTaskQueue = null;
    }
}
