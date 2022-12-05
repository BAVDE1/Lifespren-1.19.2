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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class TestBlock extends Block {
    //todo: use blockstates, currently these values are shared for all blocks
    //private boolean drawing;
    public static final BooleanProperty DRAWING = BooleanProperty.create("drawing");
    private BlockPos targetPos;
    private int i;

    public TestBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(DRAWING, false));
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        //initialise / reset data
        //this.drawing = BlockStateProperties. BooleanProperty.create("drawing");
        this.targetPos = null;
        this.i = 0;
    }

    /**
     * range=3    B=block
     * |❌| |✔| |✔| |✔| |B| |✔| |✔| |✔| |❌|
     */

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //DEBUG
        if (player.getMainHandItem().getItem() == Items.ARROW && !level.isClientSide) {
            level.setBlock(pos, state.setValue(DRAWING, false), 3);
            //state.setValue(DRAWING, false);
            debug("switched to", state);
            return InteractionResult.SUCCESS;
        }
        if (player.getMainHandItem().getItem() == Items.ACACIA_BOAT && !level.isClientSide) {
            debug("currently", state);
            return InteractionResult.SUCCESS;
        }

        if (!state.getValue(DRAWING) && !level.isClientSide) {
            debug("activated", state);
            activate(state, level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Must be called on server side
     */
    public void activate(BlockState state, Level level, BlockPos pos) {
        int hRange = 9; //horizontal range
        int vRange = 9; //vertical range

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
            level.setBlock(pos, state.setValue(DRAWING, true), 3);
            //state.setValue(DRAWING, true);
            this.targetPos = validBlockPos.get((int) Math.floor(Math.random() * validBlockPos.size()));
            debug("scheduled tick", state);
            level.scheduleTick(pos, this, 0);
        } else {
            debug("none nearby", state);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource pRandom) {
        //debug("tick called"); //annoying af
        if (state.getValue(DRAWING)) {
            drawLine(state, pos, level);
        }
    }

    private void drawLine(BlockState state, BlockPos pos, ServerLevel level) {
        // vector of block
        Vec3 v1 = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        // vector of target block
        Vec3 v2 = new Vec3(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());

        // vector of v1, v2
        Vec3 v3 = new Vec3(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);

        // scalar of vector (v3) to 10 particles per block (i think)
        double div = 10 / (v3.length() * 100);
        int i = this.i;
        double mul = div * i;

        Vec3 vLine = new Vec3(mul * v3.x, mul * v3.y, mul * v3.z);
        spawnLineParticle(vLine, pos);
        this.i = i + 1;

        if (mul >= 1) {
            level.setBlock(pos, state.setValue(DRAWING, false), 3);
            //state.setValue(DRAWING, false);
            this.i = 0;
            debug("finished drawing", state);
        } else {
            level.scheduleTick(pos, this, 1);
        }
    }

    public void spawnLineParticle(Vec3 vec3, BlockPos pos) {
        double x = pos.getX() + vec3.x + 0.5;
        double y = pos.getY() + vec3.y + 0.5;
        double z = pos.getZ() + vec3.z + 0.5;

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DRAWING);
        super.createBlockStateDefinition(builder);
    }

    public static void debug(String string, BlockState state) {
        System.out.println(string + ": " + state.getValue(DRAWING));
    }
}
