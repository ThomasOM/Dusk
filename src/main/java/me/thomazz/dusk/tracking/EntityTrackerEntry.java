package me.thomazz.dusk.tracking;

import lombok.Getter;
import lombok.Setter;
import me.thomazz.dusk.util.Area;
import me.thomazz.dusk.util.Constants;

/**
 * Tracking unit to determine the position of an entity on the client.
 * Uses an area for approximation in the case multiple positions are possible.
 */
@Getter
@Setter
public class EntityTrackerEntry {
    private final Area rootBase = new Area(); // First ping interpolation target area when still uncertain
    private final Area base = new Area(); // Area containing all possible interpolation targets from client
    private final Area position = new Area(); // Area containing all possible client positions from client

    private int interpolation; // Interpolation ticks
    private boolean certain; // If certain the client has received the interpolation target

    public EntityTrackerEntry(double x, double y, double z) {
        this.rootBase.set(x, y, z);
        this.base.set(x, y, z);
        this.position.set(x, y, z);
    }

    public void move(double dx, double dy, double dz) {
        this.rootBase.add(dx, dy, dz);
        this.base.addCoord(dx, dy, dz);

        this.interpolation = 3;
        this.certain = false;
    }

    public void teleport(double x, double y, double z) {
        double errorH = Constants.MIN_TELEPORT_HORIZONTAL;
        double errorV = Constants.MIN_TELEPORT_VERTICAL;

        this.rootBase.set(x, y, z);
        this.base.contain(x, y, z);

        // If the distance is too close to the client position it is possible for the base to remain unchanged
        if (this.position.distanceX(x) < errorH && this.position.distanceY(y) < errorV && this.position.distanceZ(z) < errorH) {
            this.rootBase.expand(errorH, errorV, errorH);
            this.base.expand(errorH, errorV, errorH);
        }

        this.interpolation = 3;
        this.certain = false;
    }

    // Marks the interpolation target as certainly received by the client
    public void markCertain() {
        this.certain = true;
        this.base.set(this.rootBase);
    }

    // Client-side interpolation
    public void interpolate() {
        /*
        If uncertain we need to assume all cases for interpolation or no interpolation.
        By including the interpolation target in the position all scenarios from 3 to 0 interpolation ticks are handled.
        We can start shrinking the position area once we are certain the client has received the target.
         */
        if (!this.certain) {
            this.position.contain(this.base);
            return;
        }

        if (this.interpolation > 0) {
            this.position.interpolate(this.base, this.interpolation--);
        }
    }
}
