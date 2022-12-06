package com.bavde1.lifespren.block.custom;

import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.util.ModTags;
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
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/* todo:
    block texture (animate texture)
    make place-able on floor, ceiling or wall
    make custom particles for bonemealing plants
    make green flame particles
 */

public class LifesprenLantern extends Block {
    protected final RandomSource random = RandomSource.create();
    //todo: currently these values are shared for all blocks, blockstates & tags(requires blockentity) could fix
    private BlockPos targetPos;
    private boolean drawing;
    private int lineProgress;

    public LifesprenLantern(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        //initialise / reset data
        this.drawing = false;
        this.targetPos = null;
        this.lineProgress = 0;
    }

    /**
     * RANGE EXAMPLE:
     * range=3    B=block
     * |❌| |✔| |✔| |✔| |B| |✔| |✔| |✔| |❌|
     */

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //DEBUG
        if (player.getMainHandItem().getItem() == Items.ARROW && !level.isClientSide) {
            this.drawing = false;
            //state.setValue(DRAWING, false);
            debug("switched to");
            return InteractionResult.SUCCESS;
        }
        if (player.getMainHandItem().getItem() == Items.ACACIA_BOAT && !level.isClientSide) {
            debug("currently");
            return InteractionResult.SUCCESS;
        }

        if (!this.drawing && !level.isClientSide) {
            debug("activated");
            activate(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Must be called on server side
     */
    public void activate(Level level, BlockPos pos) {
        int hRange = 9; //horizontal range
        int vRange = 9; //vertical range

        // detect & filter nearby blocks
        ArrayList<BlockPos> validBlockPos = new ArrayList<>();
        for (BlockPos currentBlockPos : BlockPos.betweenClosed(pos.getX() - hRange, pos.getY() - vRange, pos.getZ() - hRange, pos.getX() + hRange, pos.getY() + vRange, pos.getZ() + hRange)) {
            Block currentBlock = level.getBlockState(currentBlockPos.immutable()).getBlock();
            //if block is bonemeal-able add to list
            if (isValidBonemealableBlock(level, currentBlockPos, currentBlock, level.getBlockState(currentBlockPos), false)) {
                validBlockPos.add(currentBlockPos.immutable());
            }
        }

        // tick block if can
        if (!validBlockPos.isEmpty()) {
            this.drawing = true;
            //selects random item from list
            this.targetPos = validBlockPos.get((int) Math.floor(Math.random() * validBlockPos.size()));
            debug("scheduled tick");
            level.scheduleTick(pos, this, 0);
        } else {
            debug("none nearby");
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (this.drawing) {
            drawLine(state, pos, level, randomSource);
        }
    }

    private void drawLine(BlockState state, BlockPos pos, ServerLevel level, RandomSource randomSource) {
        // this block
        Vec3 v1 = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        // target block
        Vec3 v2 = new Vec3(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());

        // vector of v1, v2
        Vec3 v3 = new Vec3(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);

        // scalar of vector (v3) to 10 particles per block (i think)
        double div = 10 / (v3.length() * 100);
        int i = this.lineProgress;
        double mul = div * i;

        Vec3 vLine = new Vec3(mul * v3.x, mul * v3.y, mul * v3.z);
        spawnLineParticle(vLine, pos);
        this.lineProgress = i + 1;

        if (mul >= 1) {
            this.drawing = false;
            this.lineProgress = 0;
            performBonemealOnTargetBlock(level, randomSource);
            debug("finished drawing");
        } else {
            level.scheduleTick(pos, this, 1);
        }
    }

    private void performBonemealOnTargetBlock(ServerLevel level, RandomSource randomSource) {
        BlockPos targetBlockPos = this.targetPos;
        Block targetBlock = level.getBlockState(targetBlockPos).getBlock();
        if (isValidBonemealableBlock(level, targetBlockPos, targetBlock, level.getBlockState(targetBlockPos), false)) {
            ((BonemealableBlock) targetBlock).performBonemeal(level, randomSource, this.targetPos, level.getBlockState(this.targetPos));
            spawnGrowthParticle(targetBlockPos);
        }
    }

    private boolean isValidBonemealableBlock(Level level, BlockPos blockPos, Block block, BlockState blockState, boolean isClient) {
        return block instanceof BonemealableBlock
                && ((BonemealableBlock) block).isValidBonemealTarget(level, blockPos, blockState, isClient) &&
                blockState.is(ModTags.Blocks.LIFESPREN_LANTERN_BONEMEALABLE_BLOCKS);
    }

    public void spawnLineParticle(Vec3 vec3, BlockPos pos) {
        double x = pos.getX() + vec3.x + 0.5;
        double y = pos.getY() + vec3.y + getParticleOffset();
        double z = pos.getZ() + vec3.z + 0.5;

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0);
        }
    }

    public void spawnGrowthParticle(BlockPos pos) {
        for (int i = 0; i < 10; ++i) {
            int div = 7;
            double sX = (this.random.nextFloat() * 2.0F - 1.0F) / div;
            double sY = (this.random.nextFloat() * 2.0F - 1.0F) / div;
            double sZ = (this.random.nextFloat() * 2.0F - 1.0F) / div;

            double pX = pos.getX() + 0.5;
            double pY = pos.getY() + getParticleOffset();
            double pZ = pos.getZ() + 0.5;

            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().particleEngine.createParticle(ModParticles.TRAIL_PARTICLES.get(), pX, pY, pZ, sX, sY + 0.2D, sZ);
            }
        }
    }

    private double getParticleOffset() {
        //todo: change this depending on how lantern is placed
        return 0.3;
    }

    public void debug(String string) {
        //System.out.println(string);
    }
}
