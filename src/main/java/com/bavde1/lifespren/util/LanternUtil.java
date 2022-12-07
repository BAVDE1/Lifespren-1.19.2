package com.bavde1.lifespren.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class LanternUtil {

    /**
     * Returns all block positions withing range as an Iterable of BlockPos
     */
    public static Iterable<BlockPos> getBlockPosInRange(BlockPos pos, int hRange, int vRange) {
        return BlockPos.betweenClosed(pos.getX() - hRange, pos.getY() - vRange, pos.getZ() - hRange, pos.getX() + hRange, pos.getY() + vRange, pos.getZ() + hRange);
    }

    /**
     * Returns block at given block position
     */
    public static Block getBlockAtPos(Level level, BlockPos pos) {
        return level.getBlockState(pos.immutable()).getBlock();
    }

    /**
     * Returns a <strong>random blockPos from an array of blockPos</strong><br>
     * Returns null if array is empty
     */
    public static BlockPos getRandomBlockPosFromArray(ArrayList<BlockPos> arrayList) {
        return arrayList.isEmpty() ? null : arrayList.get((int) Math.floor(Math.random() * arrayList.size()));
    }

    /**
     * Returns vector of 2 block positions<br>
     * <strong>Initial point</strong> of returned vector is p1<br>
     * <strong>Terminal point</strong> of returned vector is p2
     */
    public static Vec3 getVecFrom2BlockPos(BlockPos p1, BlockPos p2) {
        // p1
        Vec3 v1 = new Vec3(p1.getX(), p1.getY(), p1.getZ());
        // p2
        Vec3 v2 = new Vec3(p2.getX(), p2.getY(), p2.getZ());

        // vector of v1, v2
        return new Vec3(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
    }

    /**
     * Returns scalar of vector to <strong>10 points per block</strong> no matter the length<br>
     */
    public static double getVecScalarForStraightLine(Vec3 v3) {
        return 10 / (v3.length() * 100);
    }

    /**
     * Returns scalar vector point of 'i'<br>
     * <strong>Example:</strong> say v3.x is 3, and 'i' is 0.5:  0.5 * 3 = 1.5 (essentially 'i' is halving in this example)
     */
    public static Vec3 getNewVecForStraightLine(Vec3 v3, double i) {
        return new Vec3(i * v3.x, i * v3.y, i * v3.z);
    }
}
