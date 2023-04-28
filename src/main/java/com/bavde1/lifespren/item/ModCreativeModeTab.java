package com.bavde1.lifespren.item;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.block.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LifesprenMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTab {
    public static CreativeModeTab LIFESPREN_TAB;

    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        LIFESPREN_TAB = event.registerCreativeModeTab(new ResourceLocation(LifesprenMod.MOD_ID, "lifespren_tab"),
                builder -> builder.icon(() -> new ItemStack(ModBlocks.LIFESPREN_LANTERN.get())).title(Component.translatable("Lifespren Tab")).build());
    }
}
