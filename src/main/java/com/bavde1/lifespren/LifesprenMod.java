package com.bavde1.lifespren;

import com.bavde1.lifespren.entity.ModEntityTypes;
import com.bavde1.lifespren.entity.client.LifesprenRenderer;
import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.sound.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

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

        // Register the commonSetup method for modloading
        eventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    // for common setup
    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        // for client setup
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            //render entities
            EntityRenderers.register(ModEntityTypes.LIFESPREN_MOB.get(), LifesprenRenderer::new);
        }
    }
}
