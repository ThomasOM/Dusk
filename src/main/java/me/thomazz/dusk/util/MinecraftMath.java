package me.thomazz.dusk.util;

import lombok.experimental.UtilityClass;
import org.joml.Vector3d;

/**
 * Ray intercept calculation methods from the Minecraft client
 */
@UtilityClass
public class MinecraftMath {
    public Vector3d calculateIntercept(Area area, Vector3d pos, Vector3d ray) {
        Vector3d vec3 = MinecraftMath.getIntermediateWithXValue(pos, ray, area.minX);
        Vector3d vec31 = MinecraftMath.getIntermediateWithXValue(pos, ray, area.maxX);
        Vector3d vec32 = MinecraftMath.getIntermediateWithYValue(pos, ray, area.minY);
        Vector3d vec33 = MinecraftMath.getIntermediateWithYValue(pos, ray, area.maxY);
        Vector3d vec34 = MinecraftMath.getIntermediateWithZValue(pos, ray, area.minZ);
        Vector3d vec35 = MinecraftMath.getIntermediateWithZValue(pos, ray, area.maxZ);
        if(!MinecraftMath.isVecInYZ(area, vec3)) {
            vec3 = null;
        }

        if(!MinecraftMath.isVecInYZ(area, vec31)) {
            vec31 = null;
        }

        if(!MinecraftMath.isVecInXZ(area, vec32)) {
            vec32 = null;
        }

        if(!MinecraftMath.isVecInXZ(area, vec33)) {
            vec33 = null;
        }

        if(!MinecraftMath.isVecInXY(area, vec34)) {
            vec34 = null;
        }

        if(!MinecraftMath.isVecInXY(area, vec35)) {
            vec35 = null;
        }

        Vector3d vec36 = null;
        if(vec3 != null) {
            vec36 = vec3;
        }

        if(vec31 != null && (vec36 == null || pos.distanceSquared(vec31) < pos.distanceSquared(vec36))) {
            vec36 = vec31;
        }

        if(vec32 != null && (vec36 == null || pos.distanceSquared(vec32) < pos.distanceSquared(vec36))) {
            vec36 = vec32;
        }

        if(vec33 != null && (vec36 == null || pos.distanceSquared(vec33) < pos.distanceSquared(vec36))) {
            vec36 = vec33;
        }

        if(vec34 != null && (vec36 == null || pos.distanceSquared(vec34) < pos.distanceSquared(vec36))) {
            vec36 = vec34;
        }

        if(vec35 != null && (vec36 == null || pos.distanceSquared(vec35) < pos.distanceSquared(vec36))) {
            vec36 = vec35;
        }

        return vec36;
    }

    private boolean isVecInYZ(Area area, Vector3d vec) {
        return vec != null && vec.y >= area.minY && vec.y <= area.maxY && vec.z >= area.minZ && vec.z <= area.maxZ;
    }

    private boolean isVecInXZ(Area area, Vector3d vec) {
        return vec != null && vec.x >= area.minX && vec.x <= area.maxX && vec.z >= area.minZ && vec.z <= area.maxZ;
    }

    private boolean isVecInXY(Area area, Vector3d vec) {
        return vec != null && vec.x >= area.minX && vec.x <= area.maxX && vec.y >= area.minY && vec.y <= area.maxY;
    }

    public Vector3d getIntermediateWithXValue(Vector3d vec1, Vector3d vec2, double limit) {
        double d0 = vec2.x - vec1.x;
        double d1 = vec2.y - vec1.y;
        double d2 = vec2.z - vec1.z;
        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (limit - vec1.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    public Vector3d getIntermediateWithYValue(Vector3d vec1, Vector3d vec2, double limit) {
        double d0 = vec2.x - vec1.x;
        double d1 = vec2.y - vec1.y;
        double d2 = vec2.z - vec1.z;
        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (limit - vec1.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    public Vector3d getIntermediateWithZValue(Vector3d vec1, Vector3d vec2, double limit) {
        double d0 = vec2.x - vec1.x;
        double d1 = vec2.y - vec1.y;
        double d2 = vec2.z - vec1.z;
        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (limit - vec1.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    public Vector3d getLookVector(float yaw, float pitch) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);

        return new Vector3d(f1 * f2, f3, f * f2);
    }
}
