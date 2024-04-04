package me.thomazz.reach.util;

import lombok.experimental.UtilityClass;

/**
 * Constants used in the Minecraft client
 */
@UtilityClass
public class Constants {
    // Combat
    public static final double RAY_LENGTH = 6.0D; // Max server reach cutoff
    public static final double MAX_RANGE = 3.0D;
    public static final float COLLISION_BORDER_SIZE = 0.1F;

    // Movement
    public final double FAST_MATH_ERROR = Constants.MAX_RANGE / 4096.0D; // Error for sin table of 4096 and max range
    public final double MIN_MOVE_UPDATE = 9.0E-4D; // In player position send logic
    public final double MIN_MOVE_UPDATE_ROOT = Math.sqrt(Constants.MIN_MOVE_UPDATE);

    // Entity move
    public final double MIN_TELEPORT_HORIZONTAL = 0.03125D; // See packet listener
    public final double MIN_TELEPORT_VERTICAL = 0.015625D;

    // Player boxes
    public final float PLAYER_BOX_WIDTH = 0.6F;
    public final float PLAYER_BOX_HEIGHT = 1.8F;

    // Timing
    public final int MAX_CATCHUP_TICKS = 10;
    public final long TICK_MILLIS = 50L;
}
