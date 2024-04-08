package me.thomazz.dusk.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Simple container class for position and rotation combinations
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Location {
    private Vector3d pos;
    private Vector2f rot;

    public Location() {
        this(new Vector3d(), new Vector2f());
    }

    public Location(double x, double y, double z, float yaw, float pitch) {
        this(new Vector3d(x, y, z), new Vector2f(yaw, pitch));
    }

    public Location setPos(double x, double y, double z) {
        if (this.pos == null) {
            this.pos = new Vector3d();
        }

        this.pos.set(x, y, z);
        return this;
    }

    public Location setRot(float yaw, float pitch) {
        if (this.rot == null) {
            this.rot = new Vector2f();
        }

        this.rot.set(yaw, pitch);
        return this;
    }

    public Location set(double x, double y, double z, float yaw, float pitch) {
        this.setPos(x, y, z);
        this.setRot(yaw, pitch);
        return this;
    }

    public Location setPos(Vector3dc vec) {
        this.setPos(vec.x(), vec.y(), vec.z());
        return this;
    }

    public Location setRot(Vector2fc vec) {
        this.setRot(vec.x(), vec.y());
        return this;
    }

    public Location set(Location location) {
        this.setPos(location.pos != null ? location.pos : new Vector3d());
        this.setRot(location.rot != null ? location.rot : new Vector2f());
        return this;
    }

    public boolean hasPos() {
        return this.pos != null;
    }

    public boolean hasRot() {
        return this.rot != null;
    }
}
