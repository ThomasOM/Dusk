package me.thomazz.reach;

import me.thomazz.reach.ping.PingTaskScheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PingTaskSchedulerTest {
    private PingTaskScheduler scheduler;

    @BeforeAll
    public void setup() {
        this.scheduler = new PingTaskScheduler();
    }

    @Test
    @Order(1)
    public void testTaskOrder() {
        Runnable runnable1 = mock(Runnable.class);
        Runnable runnable2 = mock(Runnable.class);
        Runnable runnable3 = mock(Runnable.class);

        this.scheduler.onPingSendEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleStartTask(runnable1);
        this.scheduler.scheduleStartTask(runnable2);
        this.scheduler.scheduleStartTask(runnable3);
        this.scheduler.onPingSendEnd();

        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        InOrder inOrder = inOrder(runnable1, runnable2, runnable3);
        inOrder.verify(runnable1, times(1)).run();
        inOrder.verify(runnable2, times(1)).run();
        inOrder.verify(runnable3, times(1)).run();
    }

    @Test
    @Order(2)
    public void testTaskOrderAlternate() {
        Runnable runnable1 = mock(Runnable.class);
        Runnable runnable2 = mock(Runnable.class);
        Runnable runnable3 = mock(Runnable.class);

        this.scheduler.onPingSendEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleStartTask(runnable1);
        this.scheduler.scheduleStartTask(runnable2);
        this.scheduler.scheduleStartTask(runnable3);

        this.scheduler.onPongReceiveStart();
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveEnd();

        InOrder inOrder = inOrder(runnable1, runnable2, runnable3);
        inOrder.verify(runnable1, times(1)).run();
        inOrder.verify(runnable2, times(1)).run();
        inOrder.verify(runnable3, times(1)).run();
    }


    @Test
    @Order(3)
    public void testMultiplePingTasks() {
        Runnable runnable1 = mock(Runnable.class);
        Runnable runnable2 = mock(Runnable.class);
        Runnable runnable3 = mock(Runnable.class);

        this.scheduler.onPingSendEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleStartTask(runnable1);
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleStartTask(runnable2);
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        this.scheduler.onPingSendStart();
        this.scheduler.scheduleStartTask(runnable3);
        this.scheduler.onPingSendEnd();
        this.scheduler.onPongReceiveStart();
        this.scheduler.onPongReceiveEnd();

        InOrder inOrder = inOrder(runnable1, runnable2, runnable3);
        inOrder.verify(runnable1, times(1)).run();
        inOrder.verify(runnable2, times(1)).run();
        inOrder.verify(runnable3, times(1)).run();
    }
}
