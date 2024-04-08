package me.thomazz.reach.tracking;

import lombok.Getter;
import lombok.Setter;
import me.thomazz.reach.util.Area;
import me.thomazz.reach.util.Constants;

/**
 * Tracking unit to determine the position of an entity on the client.
 * Uses an area for approximation in the case multiple positions are possible.
 */
@Getter
@Setter
public class EntityTrackerEntry {
    private final Area serverBase = new Area(); // Last sent interpolation target by server
    private final Area clientBase = new Area(); // Area containing all possible interpolation targets from client
    private final Area position = new Area(); // Area containing all possible client positions from client

    private int interpolation; // Interpolation ticks
    private boolean certain; // If certain the client has received the interpolation target

    public EntityTrackerEntry(double x, double y, double z) {
        this.serverBase.set(x, y, z);
        this.clientBase.set(x, y, z);
        this.position.set(x, y, z);
    }

    public void move(double dx, double dy, double dz) {
        this.serverBase.add(dx, dy, dz);
        this.clientBase.addCoord(dx, dy, dz);

        this.interpolation = 3;
        this.certain = false;
    }

    public void teleport(double x, double y, double z) {
        double errorH = Constants.MIN_TELEPORT_HORIZONTAL;
        double errorV = Constants.MIN_TELEPORT_VERTICAL;

        this.serverBase.set(x, y, z);
        this.clientBase.addCoord(x, y, z);

        // If the distance is too close to the client position it is possible for the base to remain unchanged
        if (this.position.distanceX(x) < errorH && this.position.distanceY(y) < errorV && this.position.distanceZ(z) < errorH) {
            this.serverBase.expand(errorH, errorV, errorH);
            this.clientBase.expand(errorH, errorV, errorH);
        }

        this.interpolation = 3;
        this.certain = false;
    }

    // Marks the interpolation target as certainly received by the client, so server and client are the same
    public void markCertain() {
        this.certain = true;
        this.clientBase.set(this.serverBase);
    }

    // Client-side interpolation
    public void interpolate() {
        /*
        If uncertain we need to assume all cases for interpolation or no interpolation.
        By including the interpolation target in the position all scenarios from 3 to 0 interpolation ticks are handled.
        We can start shrinking the position area once we are certain the client has received the target.
         */
        if (!this.certain) {
            this.position.contain(this.clientBase);
            return;
        }

        if (this.interpolation > 0) {
            this.position.interpolate(this.clientBase, this.interpolation--);
        }
    }
}
