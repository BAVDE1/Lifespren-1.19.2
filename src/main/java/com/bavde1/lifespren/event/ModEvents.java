package com.bavde1.lifespren.event;


import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.entity.ModEntityTypes;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.particle.custom.GreenFlameParticle;
import com.bavde1.lifespren.particle.custom.TrailParticle;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = LifesprenMod.MOD_ID)
    public static class ForgeEvents {
        //normal world events
    }

    @Mod.EventBusSubscriber(modid = LifesprenMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
                    GreenFlameParticle.Provider::new);
        }
    }
}
