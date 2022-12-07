package com.bavde1.lifespren.block.custom;

import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/* todo:
    organise functionality ready for new augments, (perhaps into different classes)
    currently these values are shared for all blocks, blockstates & tags(requires blockentity) could fix
    block gui for augments
 */

public class LifesprenLantern extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape AABB = Shapes.or(Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D), Block.box(6.0D, 8.0D, 6.0D, 10.0D, 10.0D, 10.0D));
    protected static final VoxelShape AABB_HANGING = Shapes.or(Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D), Block.box(6.0D, 12.0D, 6.0D, 10.0D, 14.0D, 10.0D));

    private BlockPos targetPos;
    private boolean drawing;
    private int lineProgress;

    public LifesprenLantern(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, Boolean.FALSE).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        //initialise / reset data
        this.drawing = false;
        this.targetPos = null;
        this.lineProgress = 0;
        level.scheduleTick(pos, this, 1);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());

        for(Direction direction : pContext.getNearestLookingDirections()) {
            if (direction.getAxis() == Direction.Axis.Y) {
                BlockState blockstate = this.defaultBlockState().setValue(HANGING, direction == Direction.UP);
                if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
                    return blockstate.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
                }
            }
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pState.getValue(HANGING) ? AABB_HANGING : AABB;
    }

    /**
     * RANGE EXAMPLE:
     * range=3    B=block
     * |❌| |✔| |✔| |✔| |B| |✔| |✔| |✔| |❌|
     */

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!this.drawing && !level.isClientSide) {
            activate(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Must be called on server side
     */
    public void activate(Level level, BlockPos pos) {
        int hRange = 7; //horizontal range
        int vRange = 7; //vertical range

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
            level.scheduleTick(pos, this, 0);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (this.drawing) {
            drawLine(state, pos, level, randomSource);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource randomSource) {
        if (!this.drawing) {
            spawnFlameParticle(level, state, pos, randomSource);
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
        spawnLineParticle(vLine, state, pos);
        this.lineProgress = i + 1;

        if (mul >= 1) {
            this.drawing = false;
            this.lineProgress = 0;
            performBonemealOnTargetBlock(level, randomSource);
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

    public void spawnLineParticle(Vec3 vec3, BlockState state, BlockPos pos) {
        double x = pos.getX() + vec3.x + 0.5;
        double y = pos.getY() + vec3.y + getParticleOffset(state);
        double z = pos.getZ() + vec3.z + 0.5;

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.createParticle(ModParticles.GREEN_LINE_PARTICLE.get(), x, y, z, 0, 0, 0);
        }
    }

    public void spawnGrowthParticle(BlockPos pos) {
        for (int i = 0; i < 16; ++i) {
            double pX = pos.getX() + Math.random();
            double pY = pos.getY() + Math.random();
            double pZ = pos.getZ() + Math.random();

            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().particleEngine.createParticle(ModParticles.GREEN_LINE_PARTICLE.get(), pX, pY, pZ, 0, 0, 0);
            }
        }
    }

    public void spawnFlameParticle(Level level, BlockState state, BlockPos pos, RandomSource random) {
        double pX = pos.getX() + 0.5;
        double pY = pos.getY() + getParticleOffset(state);
        double pZ = pos.getZ() + 0.5;

        if (random.nextFloat() < 0.2F) {
            level.playLocalSound(pX, pY, pZ, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
        }
        level.addParticle(ModParticles.GREEN_FLAME_PARTICLE.get(), pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        level.addParticle(ModParticles.SMALL_GREEN_FLAME_PARTICLE.get(), pX + 0.09, pY - 0.095, pZ + 0.09, 0.0D, 0.0D, 0.0D);
    }

    private double getParticleOffset(BlockState state) {
        return getConnectedDirection(state) == Direction.UP ? 0.35 : 0.6;
    }

    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.DESTROY;
    }

    protected static Direction getConnectedDirection(BlockState pState) {
        return pState.getValue(HANGING) ? Direction.DOWN : Direction.UP;
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        Direction direction = getConnectedDirection(pState).getOpposite();
        return Block.canSupportCenter(pLevel, pPos.relative(direction), direction.getOpposite());
    }

    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return getConnectedDirection(pState).getOpposite() == pDirection && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(WATERLOGGED, HANGING);
    }
}
