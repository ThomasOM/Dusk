package me.thomazz.dusk;

import me.thomazz.dusk.check.CheckRegistry;
import me.thomazz.dusk.check.impl.TimingCheck;
import me.thomazz.dusk.player.PlayerData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
public class TimingCheckTest {
    @Mock private DuskPlugin pluginMock;
    @Mock private PlayerData playerDataMock;
    private TimingCheck timing;

    @BeforeAll
    public void setupAll() {
        CheckRegistry.init();
    }

    @BeforeEach
    public void setup() {
        when(this.playerDataMock.getPlugin()).thenReturn(this.pluginMock);
        when(this.playerDataMock.getLoginTime()).thenReturn(0L);
        this.timing = new TimingCheck(this.playerDataMock);
    }

    @Test
    @Order(1)
    public void testTimingPass() {
        when(this.pluginMock.getCurrentServerTime()).thenReturn(50L);
        this.timing.ping(50L);
        this.timing.onClientTick();

        when(this.pluginMock.getCurrentServerTime()).thenReturn(100L);
        this.timing.ping(100L);
        this.timing.onClientTick();

        verify(this.pluginMock, never()).callEvent(any());
    }

    @Test
    @Order(2)
    public void testTimingFail() {
        when(this.pluginMock.getCurrentServerTime()).thenReturn(50L);
        this.timing.ping(50L);
        this.timing.onClientTick();

        when(this.pluginMock.getCurrentServerTime()).thenReturn(100L);
        this.timing.ping(100L);
        this.timing.onClientTick();
        this.timing.onClientTick();

        verify(this.pluginMock, times(1)).callEvent(any());
    }
}
