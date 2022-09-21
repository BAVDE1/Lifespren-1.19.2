package com.bavde1.lifespren.entity.client;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LifesprenModel extends AnimatedGeoModel<LifesprenEntity> {

    @Override
    public ResourceLocation getModelResource(LifesprenEntity object) {
        return new ResourceLocation(LifesprenMod.MOD_ID, "geo/lifespren.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LifesprenEntity object) {
        return new ResourceLocation(LifesprenMod.MOD_ID, "textures/entity/lifespren_mob_texture.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LifesprenEntity animatable) {
        return new ResourceLocation(LifesprenMod.MOD_ID, "animations/lifespren.animation.json");
    }
}
