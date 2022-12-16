package com.bavde1.lifespren.util.vector;

public class VectorUtils {

    public static IVec3fRead cross(IVec3fRead left, IVec3fRead right, IVec3f dest) {
        dest.set(
                left.getY() * right.getZ() - left.getZ() * right.getY(),
                right.getX() * left.getZ() - right.getZ() * left.getX(),
                left.getX() * right.getY() - left.getY() * right.getX()
        );
        return dest;
    }

    public static IVec3fRead cross(float lx, float ly, float lz, float rx, float ry, float rz, IVec3f dest) {
        dest.set(
                ly * rz - lz * ry,
                rx * lz - rz * lx,
                lx * ry - ly * rx
        );
        return dest;
    }
}
