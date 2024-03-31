package me.thomazz.reach.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Utility class to define a 3D area as an axis-aligned bounding box containing some useful transformation methods.
 */
@Getter
@AllArgsConstructor
public class Area {
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public Area() {
        this(0, 0, 0);
    }

    public Area(double x, double y, double z) {
        this.set(x, y, z);
    }

    public Area(Area other) {
        this.minX = other.minX;
        this.minY = other.minY;
        this.minZ = other.minZ;
        this.maxX = other.maxX;
        this.maxY = other.maxY;
        this.maxZ = other.maxZ;
    }

    public Area contain(Area other) {
        return this.contain(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public Area contain(double x0, double y0, double z0, double x1, double y1, double z1) {
        this.minX = Math.min(x0, this.minX);
        this.minY = Math.min(y0, this.minY);
        this.minZ = Math.min(z0, this.minZ);
        this.maxX = Math.max(x1, this.maxX);
        this.maxY = Math.max(y1, this.maxY);
        this.maxZ = Math.max(z1, this.maxZ);
        return this;
    }

    public Area contain(double x, double y, double z) {
        this.contain(x, y, z, x, y, z);
        return this;
    }

    public Area set(Area other) {
        this.minX = other.minX;
        this.minY = other.minY;
        this.minZ = other.minZ;
        this.maxX = other.maxX;
        this.maxY = other.maxY;
        this.maxZ = other.maxZ;
        return this;
    }

    public Area set(double x, double y, double z) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;

        this.maxX = x;
        this.maxY = y;
        this.maxZ = z;

        return this;
    }

    public Area expand(double x, double y, double z) {
        this.add(-x, -y, -z, x, y, z);
        return this;
    }

    public void add(double x, double y, double z) {
        this.add(x, y, z, x, y, z);
    }

    public void add(double x0, double y0, double z0, double x1, double y1, double z1) {
        this.minX += x0;
        this.minY += y0;
        this.minZ += z0;

        this.maxX += x1;
        this.maxY += y1;
        this.maxZ += z1;
    }

    public Area addCoord(double x, double y, double z) {
        double x0 = x > 0 ? 0 : x;
        double y0 = y > 0 ? 0 : y;
        double z0 = z > 0 ? 0 : z;

        double x1 = x > 0 ? x : 0;
        double y1 = y > 0 ? y : 0;
        double z1 = z > 0 ? z : 0;

        this.add(x0, y0, z0, x1, y1, z1);
        return this;
    }

    public Area interpolate(Area destination, int interpolation) {
        this.minX = this.interpolate(this.minX, destination.minX, interpolation);
        this.maxX = this.interpolate(this.maxX, destination.maxX, interpolation);
        this.minY = this.interpolate(this.minY, destination.minY, interpolation);
        this.maxY = this.interpolate(this.maxY, destination.maxY, interpolation);
        this.minZ = this.interpolate(this.minZ, destination.minZ, interpolation);
        this.maxZ = this.interpolate(this.maxZ, destination.maxZ, interpolation);
        return this;
    }

    public Area inner(Area other) {
        this.minX = Math.max(other.minX, this.minX);
        this.minY = Math.max(other.minY, this.minY);
        this.minZ = Math.max(other.minZ, this.minZ);
        this.maxX = Math.min(other.maxX, this.maxX);
        this.maxY = Math.min(other.maxY, this.maxY);
        this.maxZ = Math.min(other.maxZ, this.maxZ);
        return this;
    }

    public double distanceX(double x) {
        return x >= this.minX && x <= this.maxX ? 0.0 : Math.min(Math.abs(x - this.minX), Math.abs(x - this.maxX));
    }

    public double distanceY(double y) {
        return y >= this.minY && y <= this.maxY ? 0.0 : Math.min(Math.abs(y - this.minY), Math.abs(y - this.maxY));
    }

    public double distanceZ(double z) {
        return z >= this.minZ && z <= this.maxZ ? 0.0 : Math.min(Math.abs(z - this.minZ), Math.abs(z - this.maxZ));
    }

    private double interpolate(double value, double destination, int interpolation) {
        return value + (destination - value) / (double) interpolation;
    }
}
