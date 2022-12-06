package com.bavde1.lifespren.block.entity.client;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.block.entity.LifesprenLanternEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LifesprenLanternBlockModel extends AnimatedGeoModel<LifesprenLanternEntity> {

    @Override
    public ResourceLocation getModelResource(LifesprenLanternEntity object) {
        return new ResourceLocation(LifesprenMod.MOD_ID, "geo/lifespren_lantern.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LifesprenLanternEntity object) {
        return new ResourceLocation(LifesprenMod.MOD_ID, "textures/block/lifespren_lantern.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LifesprenLanternEntity animatable) {
        return new ResourceLocation(LifesprenMod.MOD_ID, "animations/lifespren_lantern.animation.json");
    }
}
