package com.bavde1.lifespren.event;


import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.entity.ModEntityTypes;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
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
    }
}
