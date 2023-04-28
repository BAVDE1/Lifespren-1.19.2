package com.bavde1.lifespren;

import com.bavde1.lifespren.block.ModBlocks;
import com.bavde1.lifespren.block.entity.ModBlockEntities;
import com.bavde1.lifespren.entity.ModEntityTypes;
import com.bavde1.lifespren.entity.client.LifesprenRenderer;
import com.bavde1.lifespren.item.ModCreativeModeTab;
import com.bavde1.lifespren.item.ModItems;
import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.screen.LifesprenLanternScreen;
import com.bavde1.lifespren.screen.ModMenuTypes;
import com.bavde1.lifespren.sound.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(LifesprenMod.MOD_ID)
public class LifesprenMod {
    public static final String MOD_ID = "lifespren";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LifesprenMod() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntityTypes.register(eventBus);

        ModSounds.register(eventBus);
        ModParticles.register(eventBus);

        GeckoLib.initialize();

        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModItems.register(eventBus);

        ModMenuTypes.register(eventBus);

        // Register the commonSetup method for modloading
        eventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        eventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == ModCreativeModeTab.LIFESPREN_TAB) {
            event.accept(ModBlocks.LIFESPREN_LANTERN);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        // for client setup
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            //ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIFESPREN_LANTERN.get(), RenderType.cutout());
            //render entities
            EntityRenderers.register(ModEntityTypes.LIFESPREN_MOB.get(), LifesprenRenderer::new);

            MenuScreens.register(ModMenuTypes.LIFESPREN_LANTERN_MENU.get(), LifesprenLanternScreen::new);
        }
    }
}
