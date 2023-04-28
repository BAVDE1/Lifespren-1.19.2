package com.bavde1.lifespren.block.custom;

import com.bavde1.lifespren.block.entity.LifesprenLanternBlockEntity;
import com.bavde1.lifespren.block.entity.ModBlockEntities;
import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.util.LanternUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/* todo:
    use container data (or something) for targetPos, lineProgress
 */

public class LifesprenLantern extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty DRAWING = BooleanProperty.create("drawing");

    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape AABB = Shapes.or(Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D), Block.box(6.0D, 8.0D, 6.0D, 10.0D, 10.0D, 10.0D));
    protected static final VoxelShape AABB_HANGING = Shapes.or(Block.box(5.0D, 4.0D, 5.0D, 11.0D, 12.0D, 11.0D), Block.box(6.0D, 12.0D, 6.0D, 10.0D, 14.0D, 10.0D));

    public LifesprenLantern(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HANGING, Boolean.FALSE)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(DRAWING, Boolean.FALSE));
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            LifesprenLanternBlockEntity blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null) {
                blockEntity.onPlace();
            }
        }
    }

    @Nullable
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

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
            if (!player.isCrouching()) {
                if (!level.isClientSide) {
                    LifesprenLanternBlockEntity blockEntity = getBlockEntity(level, pos);
                    if (blockEntity != null) {
                        NetworkHooks.openScreen(((ServerPlayer) player), blockEntity, pos);
                    } else {
                        throw new IllegalStateException("Our Container provider is missing!");
                    }
                }
            } else {
                if (!level.isClientSide) {
                    LifesprenLanternBlockEntity blockEntity = getBlockEntity(level, pos);
                    if (blockEntity != null) {
                        blockEntity.activate(level, pos, blockEntity, state);
                    }
                }
            }
        return InteractionResult.SUCCESS;
    }

    /**
     * Must be called on server side
     */

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource randomSource) {
        if (!state.getValue(LifesprenLantern.DRAWING)) {
            spawnFlameParticle(level, state, pos, randomSource);
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

    /**
     * Block Shape
     */

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pState.getValue(HANGING) ? AABB_HANGING : AABB;
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
        pBuilder.add(WATERLOGGED, HANGING, DRAWING);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    /**
     * Block Entity
     */

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LifesprenLanternBlockEntity) {
                ((LifesprenLanternBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LifesprenLanternBlockEntity(pos, state);
    }

    private LifesprenLanternBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof LifesprenLanternBlockEntity) {
            return (LifesprenLanternBlockEntity) entity;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        //MUST be called on server side
        return !level.isClientSide ? createTickerHelper(type, ModBlockEntities.LIFESPREN_LANTERN_BLOCK_ENTITY.get(), LifesprenLanternBlockEntity::tick) : null;
    }
}
