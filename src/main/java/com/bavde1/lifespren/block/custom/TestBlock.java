package com.bavde1.lifespren.block.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.UUID;

public class TestBlock extends Block {
    private double timeStamp;
    private boolean drawing = false;
    private ArrayList<BlockPos> validPos;

    public TestBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        this.drawing = false;
    }

    /**
     * range=3    B=block
     * |❌| |✔| |✔| |✔| |B| |✔| |✔| |✔| |❌|
     */

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.getMainHandItem().getItem() == Items.ARROW) {
            this.drawing = false;
            System.out.println("switched to: " + this.drawing);
            return InteractionResult.SUCCESS;
        }
        if (player.getMainHandItem().getItem() == Items.ACACIA_BOAT) {
            System.out.println("currently: " + drawing);
            return InteractionResult.SUCCESS;
        }
        if (!this.drawing) {
            activate(state, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void activate(BlockState state, Level level, BlockPos pos) {
        int hRange = 9; //horizontal
        int vRange = 9; //vertical

        // detect & filter nearby blocks
        ArrayList<BlockPos> validBlockPos = new ArrayList<>();
        for (BlockPos blockPos : BlockPos.betweenClosed(pos.getX() - hRange, pos.getY() - vRange, pos.getZ() - hRange, pos.getX() + hRange, pos.getY() + vRange, pos.getZ() + hRange)) {
            Block block = state.getBlock();
            if (Blocks.WHEAT == block) {
                validBlockPos.add(blockPos.immutable());
            }
        }

        // tick block if can
        if (!validBlockPos.isEmpty()) {
            this.timeStamp = System.currentTimeMillis();
            this.drawing = true;
            this.validPos = validBlockPos;
            level.scheduleTick(pos, this, 20);

            System.out.println("will draw / currently: " + this.drawing);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource pRandom) {
        drawLine(pos);
        this.drawing = false;
    }

    private void drawLine(BlockPos pos) {
        BlockPos targetPos = this.validPos.get((int) Math.floor(Math.random() * this.validPos.size()));
        double v = 0.5;
        // vector of block
        Vec3 v1 = new Vec3(pos.getX() + v, pos.getY() + v, pos.getZ() + v);
        // vector of target block
        Vec3 v2 = new Vec3(targetPos.getX() + v, targetPos.getY() + v, targetPos.getZ() + v);

        // vector of v1, v2
        Vec3 v3 = new Vec3(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);

        // scalar of vector (v3)
        double div = 10 / (v3.length() * 100); // 10 particles per block (i think)
        double percent = (div) * 100;
        for (double i = div; i < 1; i += div) {
            Vec3 vLine = new Vec3(i * v3.x, i * v3.y, i * v3.z);
            spawnParticle(vLine, pos);
        }

        System.out.println("drawn / currently: " + this.drawing);
    }

    public void spawnParticle(Vec3 vec3, BlockPos pos) {
        double x = pos.getX() + vec3.x + 0.5;
        double y = pos.getY() + vec3.y + 0.5;
        double z = pos.getZ() + vec3.z + 0.5;

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0);
        }
    }
}
