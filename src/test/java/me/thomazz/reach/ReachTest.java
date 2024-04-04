package me.thomazz.reach;

import me.thomazz.reach.player.PlayerData;
import me.thomazz.reach.tracking.EntityTracker;
import me.thomazz.reach.tracking.EntityTrackerEntry;
import org.bukkit.entity.Player;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReachTest {
    @Mock private ReachPlugin pluginMock;
    @Mock private Player playerMock;

    @BeforeEach
    public void setup() {
        when(this.playerMock.getEntityId()).thenReturn(1);
    }

    @Test
    @Order(1)
    public void testMaxRange() {
        PlayerData playerData = new PlayerData(this.pluginMock, this.playerMock);
        playerData.setAccuratePosition(true);

        EntityTracker tracker = playerData.getEntityTracker();
        tracker.addEntity(2, 0.0D, 0.0D, 3.4D); // Max range, 3 blocks with 0.4 radius box
        EntityTrackerEntry entry = tracker.getEntry(2).orElseThrow(IllegalStateException::new);

        Double value = playerData.performReachCheck(entry).orElseThrow(IllegalStateException::new);
        BigDecimal dc = new BigDecimal(value);
        dc = dc.setScale(3, RoundingMode.UP); // Round last 3 digits upwards

        assertEquals(3.0D, dc.doubleValue());
    }
}
