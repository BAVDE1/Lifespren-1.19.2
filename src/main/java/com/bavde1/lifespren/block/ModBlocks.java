package com.bavde1.lifespren.block;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.block.custom.LifesprenLantern;
import com.bavde1.lifespren.item.ModCreativeModeTab;
import com.bavde1.lifespren.item.ModItems;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, LifesprenMod.MOD_ID);


    public static final RegistryObject<Block> LIFESPREN_LANTERN = registerBlock("lifespren_lantern",
            () -> new LifesprenLantern(BlockBehaviour
                    .Properties.of(Material.METAL)
                    .strength(6f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()
                    .lightLevel((BlockState) -> 10)));


    //register
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
