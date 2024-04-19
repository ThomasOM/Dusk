package me.thomazz.dusk;

import me.thomazz.dusk.check.impl.TimingCheck;
import me.thomazz.dusk.player.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimingCheckTest {
    @Mock private DuskPlugin pluginMock;
    @Mock private PlayerData playerDataMock;

    @Spy
    @InjectMocks
    private TimingCheck timingSpy;

    @BeforeEach
    public void setup() {
        when(this.playerDataMock.getPlugin()).thenReturn(this.pluginMock);
        when(this.playerDataMock.getLoginTime()).thenReturn(0L);
    }

    @Test
    @Order(1)
    public void testTimingPass() {
        when(this.pluginMock.getCurrentServerTime()).thenReturn(50L);
        this.timingSpy.ping(50L);
        this.timingSpy.onClientTick();

        when(this.pluginMock.getCurrentServerTime()).thenReturn(100L);
        this.timingSpy.ping(100L);
        this.timingSpy.onClientTick();

        verify(this.timingSpy, never()).flag(any());
    }

    @Test
    @Order(2)
    public void testTimingFail() {
        when(this.pluginMock.getCurrentServerTime()).thenReturn(50L);
        this.timingSpy.ping(50L);
        this.timingSpy.onClientTick();

        when(this.pluginMock.getCurrentServerTime()).thenReturn(100L);
        this.timingSpy.ping(100L);
        this.timingSpy.onClientTick();
        this.timingSpy.onClientTick();

        verify(this.timingSpy, times(1)).flag(any());
    }
}
