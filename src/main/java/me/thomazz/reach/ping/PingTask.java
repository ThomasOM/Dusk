package me.thomazz.reach.ping;

/**
 * Task interface for {@link PingTaskScheduler}
 */
public interface PingTask {
    default void onStart() {} // Executes on first pong received
    default void onEnd() {} // Executes on second pong received

    static PingTask of(Runnable r1, Runnable r2) {
        return new PingTask() {
            @Override
            public void onStart() {
                r1.run();
            }

            @Override
            public void onEnd() {
                r2.run();
            }
        };
    }
}
