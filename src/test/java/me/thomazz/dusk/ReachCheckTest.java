package me.thomazz.dusk;

import me.thomazz.dusk.check.CheckRegistry;
import me.thomazz.dusk.check.impl.ReachCheck;
import me.thomazz.dusk.player.PlayerData;
import me.thomazz.dusk.tracking.EntityTracker;
import me.thomazz.dusk.tracking.EntityTrackerEntry;
import me.thomazz.dusk.util.Location;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReachCheckTest {
    @Mock private PlayerData playerDataMock;
    private ReachCheck reachCheck;

    @BeforeAll
    public void setupAll() {
        CheckRegistry.init();
    }

    @BeforeEach
    public void setup() {
        this.reachCheck = new ReachCheck(this.playerDataMock);
    }

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

        ReachCheck spy = spy(this.reachCheck);
        ResultCaptor<Optional<Double>> captor = new ResultCaptor<>();
        doAnswer(captor).when(spy).performReachCheck(entry);
        spy.onClientTick();

        Optional<Double> result = captor.result;
        Double value = result.orElseThrow(IllegalStateException::new);
        BigDecimal dc = new BigDecimal(value);
        dc = dc.setScale(3, RoundingMode.UP); // Round last 3 digits upwards

        assertEquals(3.0D, dc.doubleValue());
    }

    private static class ResultCaptor<T> implements Answer {
        private T result;

        @Override
        @SuppressWarnings("unchecked")
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            this.result = (T) invocationOnMock.callRealMethod();
            return this.result;
        }
    }
}
