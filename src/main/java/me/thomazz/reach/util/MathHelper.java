package me.thomazz.reach.util;

import lombok.experimental.UtilityClass;

/**
 * Math tables from Minecraft client
 */
@UtilityClass
public class MathHelper {
    private final float[] SIN_TABLE = new float[65536];

    static {
        for (int i = 0; i < MathHelper.SIN_TABLE.length; ++i) {
            MathHelper.SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2.0D / 65536.0D);
        }
    }

    public float sin(double value) {
        return MathHelper.SIN_TABLE[(int) (value * 10430.378F) & 65535];
    }

    public float cos(double value) {
        return MathHelper.SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }
}
