package com.bavde1.lifespren.event;


import com.bavde1.lifespren.entity.ModEntityTypes;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.particle.custom.LineParticle;
import com.bavde1.lifespren.particle.custom.ModFlameParticle;
import com.bavde1.lifespren.particle.custom.TrailParticle;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

import static com.bavde1.lifespren.LifesprenMod.MOD_ID;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ForgeEvents {
        //normal world events
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        @SubscribeEvent
        public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.LIFESPREN_MOB.get(), LifesprenEntity.setAttributes());
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            Minecraft.getInstance().particleEngine.register(ModParticles.TRAIL_PARTICLE.get(),
                    TrailParticle.Provider::new);

            Minecraft.getInstance().particleEngine.register(ModParticles.GREEN_FLAME_PARTICLE.get(),
                    ModFlameParticle.SmallFlameProvider::new);
            Minecraft.getInstance().particleEngine.register(ModParticles.SMALL_GREEN_FLAME_PARTICLE.get(),
                    ModFlameParticle.SmallerFlameProvider::new);

            Minecraft.getInstance().particleEngine.register(ModParticles.GREEN_LINE_PARTICLE.get(),
                    LineParticle.LineProvider::new);
        }
    }
}
