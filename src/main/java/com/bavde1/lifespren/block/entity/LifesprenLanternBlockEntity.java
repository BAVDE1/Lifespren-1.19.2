package com.bavde1.lifespren.block.entity;

import com.bavde1.lifespren.block.custom.LifesprenLantern;
import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.screen.LifesprenLanternMenu;
import com.bavde1.lifespren.util.LanternUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class LifesprenLanternBlockEntity extends BlockEntity implements MenuProvider {
    public int nearbyValidBlockCount = 0;
    public int hRange = 6; //horizontal range
    public int vRange = 3; //vertical range
    public BlockPos targetPos;
    public double lineProgress;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> LifesprenLanternBlockEntity.this.nearbyValidBlockCount;
                case 1 -> LifesprenLanternBlockEntity.this.hRange;
                case 2 -> LifesprenLanternBlockEntity.this.vRange;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> LifesprenLanternBlockEntity.this.nearbyValidBlockCount = value;
                case 1 -> LifesprenLanternBlockEntity.this.hRange = value;
                case 2 -> LifesprenLanternBlockEntity.this.vRange = value;
            }
        }

        @Override
        public int getCount() {
            return 3; //size of data array ?
        }
    };

    public LifesprenLanternBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LIFESPREN_LANTERN_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void onPlace() {
        //initialise / reset data
        targetPos = null;
        lineProgress = 0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lifespren.lifespren_lantern.display_name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new LifesprenLanternMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
    }

    /**
     * Called when block is broken to drop its inventory
     */
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    /**
     * <strong>Growth functionality</strong>
     * <br><br>
     * Must be called on server side (called in block class)
     * <br><br>
     * RANGE EXAMPLE: <br>
     * range=3    B=block <br>
     * |❌| |✔| |✔| |✔| |B| |✔| |✔| |✔| |❌|
     */
    public void activate(Level level, BlockPos pos, LifesprenLanternBlockEntity blockEntity, BlockState state) {
        if (!isDrawing(state)) {
            ArrayList<BlockPos> validBlockPos = getNearbyValidBlocks(level, pos, hRange, vRange, true);

            //if can, set drawing & block
            if (!validBlockPos.isEmpty()) {
                setDrawingBlockState(level, pos, state, blockEntity, Boolean.TRUE);
                //selects random item from list
                blockEntity.targetPos = LanternUtil.getRandomBlockPosFromArray(validBlockPos);
            }
        }
    }

    // detect & filter nearby blocks
    public static ArrayList<BlockPos> getNearbyValidBlocks(Level level, BlockPos pos, int hRange, int vRange, boolean needCurrentlyBonemealable) {
        ArrayList<BlockPos> validBlockPos = new ArrayList<>();
        for (BlockPos currentBlockPos : LanternUtil.getBlockPosInRange(pos, hRange, vRange)) {
            Block currentBlock = LanternUtil.getBlockAtPos(level, currentBlockPos);

            if (needCurrentlyBonemealable) {
                //if block is CURRENTLY bonemeal-able, add to list
                if (LanternUtil.isValidBonemealableBlock(level, currentBlockPos, currentBlock, level.getBlockState(currentBlockPos), level.isClientSide)) {
                    validBlockPos.add(currentBlockPos.immutable());
                }
            } else {
                //if block is bonemealable at SOME stage of its age, add to list
                if (LanternUtil.isValidGrowableBlock(currentBlock, level.getBlockState(currentBlockPos))) {
                    validBlockPos.add(currentBlockPos.immutable());
                }
            }
        }

        return  validBlockPos;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LifesprenLanternBlockEntity blockEntity) {
        if (pos != null) {
            if (blockEntity.isDrawing(state)) {
                drawLine(state, pos, (ServerLevel) level, RandomSource.create(), blockEntity);
            }

            //counts nearby growable crops & adds to data
            ArrayList<BlockPos> validBlockPos = getNearbyValidBlocks(level, pos, blockEntity.hRange, blockEntity.vRange, false);
            blockEntity.nearbyValidBlockCount = validBlockPos.size();
        }
    }

    private static void drawLine(BlockState state, BlockPos pos, ServerLevel level, RandomSource randomSource, LifesprenLanternBlockEntity blockEntity) {
        if (blockEntity.targetPos != null) {
            // vector of pos, targetPos
            Vec3 v3 = LanternUtil.getVecFrom2BlockPos(pos, blockEntity.targetPos);

            double div = LanternUtil.getVecScalarForStraightLine(v3);
            blockEntity.lineProgress = blockEntity.lineProgress == 0 ? div : div * (blockEntity.lineProgress / div);
            double i = blockEntity.lineProgress;

            Vec3 vLine = LanternUtil.getNewVecForStraightLine(v3, i);
            spawnLineParticle(vLine, pos);
            blockEntity.lineProgress = blockEntity.lineProgress + div;

            if (i >= 1) {
                blockEntity.setDrawingBlockState(level, pos, state, blockEntity, Boolean.FALSE);
                //blockEntity.drawing = false;
                blockEntity.lineProgress = 0;
                performBonemealOnTargetBlock(level, randomSource, blockEntity);
            } else {
                level.scheduleTick(pos, state.getBlock(), 1);
            }
        }
    }

    private static void performBonemealOnTargetBlock(ServerLevel level, RandomSource randomSource, LifesprenLanternBlockEntity blockEntity) {
        Block targetBlock = LanternUtil.getBlockAtPos(level, blockEntity.targetPos);

        if (LanternUtil.isValidBonemealableBlock(level, blockEntity.targetPos, targetBlock, level.getBlockState(blockEntity.targetPos), false)) {
            ((BonemealableBlock) targetBlock).performBonemeal(level, randomSource, blockEntity.targetPos, level.getBlockState(blockEntity.targetPos));
            spawnGrowthParticle(blockEntity.targetPos);
        }
    }

    public static void spawnLineParticle(Vec3 vec3, BlockPos pos) {
        double x = pos.getX() + vec3.x + 0.5;
        double y = pos.getY() + vec3.y + 0.5;
        double z = pos.getZ() + vec3.z + 0.5;

        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.createParticle(ModParticles.GREEN_LINE_PARTICLE.get(), x, y, z, 0, 0, 0);
        }
    }

    public static void spawnGrowthParticle(BlockPos pos) {
        for (int i = 0; i < 16; ++i) {
            double pX = pos.getX() + Math.random();
            double pY = pos.getY() + Math.random();
            double pZ = pos.getZ() + Math.random();

            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().particleEngine.createParticle(ModParticles.GREEN_LINE_PARTICLE.get(), pX, pY, pZ, 0, 0, 0);
            }
        }
    }

    private boolean isDrawing(BlockState state) {
        return state.getValue(LifesprenLantern.DRAWING);
    }

    private void setDrawingBlockState(Level level, BlockPos pos, BlockState state, LifesprenLanternBlockEntity blockEntity, boolean value) {
        BlockPos tp = blockEntity.targetPos;

        state = state.setValue(LifesprenLantern.DRAWING, value);
        level.setBlock(pos, state, 3);

        //level.setBlock resets targetPos data to null, so reassign here
        blockEntity.targetPos = tp;
    }
}
