package com.bavde1.lifespren.block.custom;

import com.bavde1.lifespren.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.UUID;

public class TestBlock extends Block {
    public TestBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    /**
     * range=3    B=block
     * |❌| |✔| |✔| |✔| |B| |✔| |✔| |✔| |❌|
     */

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        int hRange = 9; //horizontal
        int vRange = 9; //vertical
        ArrayList<BlockPos> validBlockPos = new ArrayList<>();
        for (BlockPos blockPos : BlockPos.betweenClosed(pos.getX() - hRange, pos.getY() - vRange, pos.getZ() - hRange, pos.getX() + hRange, pos.getY() + vRange, pos.getZ() + hRange)) {
            Block block = level.getBlockState(blockPos.immutable()).getBlock();
            if (Blocks.WHEAT == block) {
                validBlockPos.add(blockPos.immutable());
            }
        }

        if (!validBlockPos.isEmpty()) {
            BlockPos targetPos = validBlockPos.get((int) Math.floor(Math.random() * validBlockPos.size()));
            double v = 0.5;
            // vector of block
            Vec3 v1 = new Vec3(pos.getX() + v, pos.getY() + v, pos.getZ() + v);
            // vector of target block
            Vec3 v2 = new Vec3(targetPos.getX() + v, targetPos.getY() + v, targetPos.getZ() + v);

            // vector of 2 positions (v1, v2)
            Vec3 v3 = new Vec3(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);

            // loop for division of vector (v3)
            double div = Math.pow(v3.length(), -2);
            for (double i = div; i < 1; i += div) {
                Vec3 vLine = new Vec3(i * v3.x, i * v3.y, i * v3.z);
                spawnParticle(vLine, pos);
            }

/*
            System.out.println("============================================================");
            System.out.println("WHEAT: " + targetPos.getX() + targetPos.getY() + targetPos.getZ() + "     (" + UUID.randomUUID() + ")");
            System.out.println("Vector: " + v3 + "            Vec length: " + v3.length());
*/
        } else {
            System.out.println("no wheat");
        }
        super.randomTick(state, level, pos, randomSource);
    }

    public void spawnParticle(Vec3 vec3, BlockPos pos) {
        double x = pos.getX() + vec3.x + 0.5;
        double y = pos.getY() + vec3.y + 0.5;
        double z = pos.getZ() + vec3.z + 0.5;

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.createParticle(ModParticles.TRAIL_PARTICLES.get(), x, y, z, 0, 0, 0);
            System.out.println("Particle" + UUID.randomUUID());
        } else {
            System.out.println("level is null");
        }
    }
}
