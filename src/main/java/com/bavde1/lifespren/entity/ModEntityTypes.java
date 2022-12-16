package com.bavde1.lifespren.entity;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LifesprenMod.MOD_ID);

    public static final RegistryObject<EntityType<LifesprenEntity>> LIFESPREN_MOB =
            ENTITY_TYPES.register("lifespren_mob",
                    () -> EntityType.Builder.of(LifesprenEntity::new, MobCategory.AMBIENT)
                            .sized(0.15f, 0.17f)
                            .build(new ResourceLocation(LifesprenMod.MOD_ID, "lifespren_mob").toString()));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
