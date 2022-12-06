package com.bavde1.lifespren.particle;

import com.bavde1.lifespren.LifesprenMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, LifesprenMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> TRAIL_PARTICLE =
            PARTICLE_TYPES.register("trail_particle", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GREEN_FLAME_PARTICLE =
            PARTICLE_TYPES.register("green_flame_particle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMALL_GREEN_FLAME_PARTICLE =
            PARTICLE_TYPES.register("small_green_flame_particle", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
