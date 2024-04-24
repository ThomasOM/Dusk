package me.thomazz.dusk.check.impl;

import me.thomazz.dusk.DuskPlugin;
import me.thomazz.dusk.check.Check;
import me.thomazz.dusk.check.CheckInfo;
import me.thomazz.dusk.check.event.flag.ReachFlagData;
import me.thomazz.dusk.player.PlayerData;
import me.thomazz.dusk.tracking.EntityTrackerEntry;
import me.thomazz.dusk.util.Area;
import me.thomazz.dusk.util.Constants;
import me.thomazz.dusk.util.MinecraftMath;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.joml.Vector3d;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@CheckInfo(name = "Reach")
public class ReachCheck extends Check {
    public ReachCheck(PlayerData data) {
        super(data);
    }

    @Override
    public void onClientTick() {
        // Only check when actually attacking
        if (!this.data.isAttacking()) {
            return;
        }

        // Get tracked entity and perform reach check
        this.data.getEntityTracker().getEntry(this.data.getLastAttacked())
            .map(this::performReachCheck)
            .map(result -> result.orElse(Double.POSITIVE_INFINITY))
            .filter(range -> range > Constants.MAX_RANGE)
            .map(ReachFlagData::new)
            .ifPresent(this::flag);
    }

    // Tries to mirror client logic as closely as possible while including a few errors
    public Optional<Double> performReachCheck(EntityTrackerEntry entry) {
        // Get position area of entity we have been tracking
        Area position = entry.getPosition();

        // Expand position area into bounding box
        float width = Constants.PLAYER_BOX_WIDTH;
        float height =  Constants.PLAYER_BOX_HEIGHT;

        Area box = new Area(position)
            .expand(width / 2.0, 0.0, width / 2.0)
            .addCoord(0.0, height, 0.0);

        // The hitbox is actually 0.1 blocks bigger than the bounding box
        float offset = Constants.COLLISION_BORDER_SIZE;
        box.expand(offset, offset, offset);

        // Compensate for fast math errors in the look vector calculations (Can remove if support not needed)
        double error = Constants.FAST_MATH_ERROR;
        box.expand(error, error, error);

        /*
        Expand the box by the root of the minimum move amount in each axis if the player was not moving the last tick.
        This is because they could have moved this amount on the client making a difference between a hit or miss.
         */
        if (!this.data.isAccuratePosition()) {
            double minMove = Constants.MIN_MOVE_UPDATE_ROOT;
            box.expand(minMove, minMove, minMove);
        }

        // Mouse input is done before any sneaking updates
        float eyeHeight = 1.62F;
        if (this.data.wasSneaking()) {
            eyeHeight -= 0.08F;
        }

        // Previous position since movement is done after attacking in the client tick
        Vector3d eye = this.data.getLocO().getPos().add(0, eyeHeight, 0, new Vector3d());

        // First check if the eye position is inside
        if (box.isInside(eye.x, eye.y, eye.z)) {
            return Optional.of(0.0D);
        }

        // Originally Minecraft uses the old yaw value for mouse intercepts, but some clients and mods fix this
        float yawO = this.data.getLocO().getRot().x;
        float yaw = this.data.getLoc().getRot().x;
        float pitch = this.data.getLoc().getRot().y;

        Vector3d viewO = MinecraftMath.getLookVector(yawO, pitch).mul(Constants.RAY_LENGTH);
        Vector3d view = MinecraftMath.getLookVector(yaw, pitch).mul(Constants.RAY_LENGTH);

        Vector3d eyeViewO = eye.add(viewO, new Vector3d());
        Vector3d eyeView = eye.add(view, new Vector3d());

        // Calculate intercepts with Minecraft ray logic
        Vector3d interceptO = MinecraftMath.calculateIntercept(box, eye, eyeViewO);
        Vector3d intercept = MinecraftMath.calculateIntercept(box, eye, eyeView);

        // Get minimum value of intercepts
        Optional<Double> result = Stream.of(interceptO, intercept)
            .filter(Objects::nonNull)
            .map(eye::distance)
            .min(Double::compare);

        // Debug broadcast
        if (DuskPlugin.DEBUG) {
            String debug = new DecimalFormat("0.000").format(result.orElse(Double.NaN));
            Bukkit.broadcastMessage(ChatColor.GRAY + this.data.getPlayer().getName() + " attack range: " + debug);
        }

        return result;
    }
}
