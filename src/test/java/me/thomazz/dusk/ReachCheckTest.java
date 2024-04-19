package me.thomazz.dusk;

import me.thomazz.dusk.check.CheckRegistry;
import me.thomazz.dusk.check.impl.ReachCheck;
import me.thomazz.dusk.player.PlayerData;
import me.thomazz.dusk.tracking.EntityTracker;
import me.thomazz.dusk.tracking.EntityTrackerEntry;
import me.thomazz.dusk.util.Location;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReachCheckTest {
    @Mock private PlayerData playerDataMock;

    @Spy
    @InjectMocks
    private ReachCheck reachCheckSpy;

    @Test
    @Order(1)
    public void testMaxRange() {
        EntityTracker tracker = new EntityTracker();
        tracker.addEntity(2, 0.0D, 0.0D, 3.4D); // Max range, 3 blocks with 0.4 radius box
        EntityTrackerEntry entry = tracker.getEntry(2).orElseThrow(IllegalStateException::new);

        when(this.playerDataMock.getEntityTracker()).thenReturn(tracker);
        when(this.playerDataMock.isAttacking()).thenReturn(true);
        when(this.playerDataMock.getLastAttacked()).thenReturn(2);
        when(this.playerDataMock.isAccuratePosition()).thenReturn(true);
        when(this.playerDataMock.getLoc()).thenReturn(new Location());
        when(this.playerDataMock.getLocO()).thenReturn(new Location());

        ResultCaptor<Optional<Double>> captor = new ResultCaptor<>();
        doAnswer(captor).when(this.reachCheckSpy).performReachCheck(entry);
        this.reachCheckSpy.onClientTick();

        Optional<Double> result = captor.result;
        Double value = result.orElseThrow(IllegalStateException::new);
        BigDecimal dc = new BigDecimal(value);
        dc = dc.setScale(3, RoundingMode.UP); // Round last 3 digits upwards

        assertEquals(3.0D, dc.doubleValue());
        verify(this.reachCheckSpy, never()).flag(any());
    }

    @SuppressWarnings({"NewClassNamingConvention", "unchecked"})
    private static class ResultCaptor<T> implements Answer<T> {
        private T result;

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            this.result = (T) invocationOnMock.callRealMethod();
            return this.result;
        }
    }
}
