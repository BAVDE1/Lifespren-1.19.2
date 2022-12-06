package com.bavde1.lifespren.block.entity;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, LifesprenMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<LifesprenLanternEntity>> LIFESPREN_LANTERN_ENTITY =
            BLOCK_ENTITIES.register("lifespren_lantern_entity", () ->
                    BlockEntityType.Builder.of(LifesprenLanternEntity::new,
                            ModBlocks.LIFESPREN_LANTERN.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
