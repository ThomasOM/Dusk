package me.thomazz.dusk;

import me.thomazz.dusk.timing.Timing;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimingTest {
    @Mock private DuskPlugin pluginMock;
    @Mock private Player playerMock;

    @Test
    @Order(1)
    public void testTimingPass() {
        Timing timing = new Timing(this.pluginMock, this.playerMock, 0L);

        when(this.pluginMock.getCurrentServerTime()).thenReturn(50L);
        timing.ping(50L);
        timing.tick();

        when(this.pluginMock.getCurrentServerTime()).thenReturn(100L);
        timing.ping(100L);
        timing.tick();

        verify(this.pluginMock, never()).callEvent(any());
    }

    @Test
    @Order(2)
    public void testTimingFail() {
        Timing timing = new Timing(this.pluginMock, this.playerMock, 0L);

        when(this.pluginMock.getCurrentServerTime()).thenReturn(50L);
        timing.ping(50L);
        timing.tick();

        when(this.pluginMock.getCurrentServerTime()).thenReturn(100L);
        timing.ping(100L);
        timing.tick();
        timing.tick();

        verify(this.pluginMock, times(1)).callEvent(any());
    }
}
