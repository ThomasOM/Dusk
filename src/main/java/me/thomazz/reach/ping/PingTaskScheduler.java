package me.thomazz.reach.ping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

/**
 * Utility that schedules tasks to be executed on a client response to server tick pings.
 */
@Getter
@RequiredArgsConstructor
public class PingTaskScheduler {
    private final Queue<Queue<PingTask>> scheduledTasks = new ArrayDeque<>();

    private boolean receivingPongs;

    @Nullable private Queue<PingTask> schedulingTaskQueue;
    @Nullable private Queue<PingTask> runningTaskQueue;

    /**
     * Schedules a runnable to execute when the response for the pings of the current server tick is received from the client.
     * <p>
     * @param task - Runnable to schedule
     */
    public void scheduleTask(PingTask task) {
        // The client can actually respond with a pong before the server tick has completed, so instantly process new tasks
        if (Objects.equals(this.runningTaskQueue, this.schedulingTaskQueue)) {
            task.onStart();
            return;
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
        this.schedulingTaskQueue = new ArrayDeque<>();
        this.scheduledTasks.add(this.schedulingTaskQueue);
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
