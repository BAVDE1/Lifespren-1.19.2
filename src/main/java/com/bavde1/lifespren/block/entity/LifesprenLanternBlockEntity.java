package com.bavde1.lifespren.block.entity;

import com.bavde1.lifespren.screen.LifesprenLanternMenu;
import com.bavde1.lifespren.util.LanternUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class LifesprenLanternBlockEntity extends BlockEntity implements MenuProvider {
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;
    public boolean drawing;
    public BlockPos targetPos;
    public double lineProgress;
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public LifesprenLanternBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.LIFESPREN_LANTERN_BLOCK_ENTITY.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return index;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 2; // size
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Lifespren lantern");
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
     * Must be called on server side
     */
    public void activate(Level level, BlockPos pos, LifesprenLanternBlockEntity blockEntity) {
        int hRange = 6; //horizontal range
        int vRange = 3; //vertical range

        // detect & filter nearby blocks
        ArrayList<BlockPos> validBlockPos = new ArrayList<>();

        for (BlockPos currentBlockPos : LanternUtil.getBlockPosInRange(pos, hRange, vRange)) {
            Block currentBlock = LanternUtil.getBlockAtPos(level, currentBlockPos);
            //if block is bonemeal-able add to list
            if (LanternUtil.isValidBonemealableBlock(level, currentBlockPos, currentBlock, level.getBlockState(currentBlockPos), level.isClientSide)) {
                validBlockPos.add(currentBlockPos.immutable());
            }
        }

        // tick block if can
        if (!validBlockPos.isEmpty()) {
            blockEntity.drawing = true;
            //selects random item from list
            blockEntity.targetPos = LanternUtil.getRandomBlockPosFromArray(validBlockPos);
        }
    }

    /**
     * Called in LifesprenLantern when block is broken to drop its inventory
     */
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public Block getBlock() {
        return this.getBlockState().getBlock();
    }

    public static void tick(Level level, BlockPos blockPos, BlockState state, LifesprenLanternBlockEntity entity) {
        if (level.isClientSide()) {
            return;
        }
    }
}
