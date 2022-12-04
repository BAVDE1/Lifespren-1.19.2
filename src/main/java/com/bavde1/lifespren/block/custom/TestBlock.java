package com.bavde1.lifespren.block.custom;

import net.minecraft.client.Minecraft;
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

public class TestBlock extends Block {
    private boolean drawing;
    private ArrayList<BlockPos> validPos;

    public TestBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        //initialise / reset data
        this.drawing = false;
        this.validPos = new ArrayList<>();
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
        //todo: this is getting called on client side, use blockstate instead of this.drawing perhaps, or no this.? idk
        if (!this.drawing) {
            System.out.println("activated");
            activate(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    public void activate(Level level, BlockPos pos) {
        int hRange = 9; //horizontal
        int vRange = 9; //vertical

        // detect & filter nearby blocks
        ArrayList<BlockPos> validBlockPos = new ArrayList<>();
        for (BlockPos currentBlockPos : BlockPos.betweenClosed(pos.getX() - hRange, pos.getY() - vRange, pos.getZ() - hRange, pos.getX() + hRange, pos.getY() + vRange, pos.getZ() + hRange)) {
            Block currentBlock = level.getBlockState(currentBlockPos.immutable()).getBlock();
            if (Blocks.WHEAT == currentBlock) {
                validBlockPos.add(currentBlockPos.immutable());
            }
        }

        // tick block if can
        if (!validBlockPos.isEmpty()) {
            this.drawing = true;
            this.validPos = new ArrayList<>(validBlockPos);
            if (!level.isClientSide) {
                System.out.println("scheduled tick");
                level.scheduleTick(pos, this.asBlock(), 1);
            }

        } else {
            System.out.println("failed: no crops nearby");
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource pRandom) {
        System.out.println("tick called");
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
            spawnLineParticle(vLine, pos);
        }

        this.drawing = false;

        System.out.println("drawn / currently: " + this.drawing);
    }

    public void spawnLineParticle(Vec3 vec3, BlockPos pos) {
        double x = pos.getX() + vec3.x + 0.5;
        double y = pos.getY() + vec3.y + 0.5;
        double z = pos.getZ() + vec3.z + 0.5;

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0);
        }
    }
}
