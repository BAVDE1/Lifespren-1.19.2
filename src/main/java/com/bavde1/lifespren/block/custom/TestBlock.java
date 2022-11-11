package com.bavde1.lifespren.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        int searchRange = 10;
        ArrayList<BlockPos> validBlockPos = new ArrayList<>();
        for (BlockPos blockPos : BlockPos.betweenClosed(pos.getX() - searchRange, pos.getY() - searchRange, pos.getZ() - searchRange, pos.getX() + searchRange, pos.getY() + searchRange, pos.getZ() + searchRange)) {
            Block block = level.getBlockState(blockPos.immutable()).getBlock();
            if (Blocks.WHEAT == block) {
                validBlockPos.add(blockPos.immutable());
            }
        }

        if (!validBlockPos.isEmpty()) {
            BlockPos targetPos = validBlockPos.get((int) Math.floor(Math.random() * validBlockPos.size()));
            targetPos.offset(0.5, 0.5, 0.5);
            System.out.println("============================================================");
            System.out.println("WHEAT: " + targetPos + "     (" + UUID.randomUUID() + ")");
        } else {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("no wheat     (" + UUID.randomUUID() + ")");
        }
        super.randomTick(state, level, pos, randomSource);
    }
}
