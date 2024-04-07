package me.thomazz.reach;

import me.thomazz.reach.ping.PingTask;
import me.thomazz.reach.ping.PingTaskScheduler;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PingTaskSchedulerTest {
    @Mock private Plugin pluginMock;
    private PingTaskScheduler scheduler;

    @BeforeEach
    public void setup() {
        this.scheduler = new PingTaskScheduler();
    }

    @Test
    @Order(1)
    public void testTaskOrder() {
        PingTask task1 = mock(PingTask.class);
        PingTask task2 = mock(PingTask.class);
        PingTask task3 = mock(PingTask.class);

        this.scheduler.onPingSendEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleTask(task1);
        this.scheduler.scheduleTask(task2);
        this.scheduler.scheduleTask(task3);
        this.scheduler.onPingSendEnd();

        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        InOrder inOrder = inOrder(task1, task2, task3, task1, task2, task3);
        inOrder.verify(task1, times(1)).onStart();
        inOrder.verify(task2, times(1)).onStart();
        inOrder.verify(task3, times(1)).onStart();
        inOrder.verify(task1, times(1)).onEnd();
        inOrder.verify(task2, times(1)).onEnd();
        inOrder.verify(task3, times(1)).onEnd();
    }

    @Test
    @Order(2)
    public void testTaskOrderAlternate() {
        PingTask task1 = mock(PingTask.class);
        PingTask task2 = mock(PingTask.class);
        PingTask task3 = mock(PingTask.class);

        this.scheduler.onPingSendEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleTask(task1);
        this.scheduler.scheduleTask(task2);
        this.scheduler.scheduleTask(task3);

        this.scheduler.onPongReceiveStart();
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveEnd();

        InOrder inOrder = inOrder(task1, task2, task3, task1, task2, task3);
        inOrder.verify(task1, times(1)).onStart();
        inOrder.verify(task2, times(1)).onStart();
        inOrder.verify(task3, times(1)).onStart();
        inOrder.verify(task1, times(1)).onEnd();
        inOrder.verify(task2, times(1)).onEnd();
        inOrder.verify(task3, times(1)).onEnd();
    }


    @Test
    @Order(3)
    public void testMultiplePingTasks() {
        PingTask task1 = mock(PingTask.class);
        PingTask task2 = mock(PingTask.class);
        PingTask task3 = mock(PingTask.class);

        this.scheduler.onPingSendEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleTask(task1);
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleTask(task2);
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleTask(task3);
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        InOrder inOrder = inOrder(task1, task2, task3, task1, task2, task3);
        inOrder.verify(task1, times(1)).onStart();
        inOrder.verify(task1, times(1)).onEnd();
        inOrder.verify(task2, times(1)).onStart();
        inOrder.verify(task2, times(1)).onEnd();
        inOrder.verify(task3, times(1)).onStart();
        inOrder.verify(task3, times(1)).onEnd();
    }
}
