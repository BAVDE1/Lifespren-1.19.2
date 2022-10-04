package com.bavde1.lifespren.entity.client;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.entity.ModEntityTypes;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.bavde1.lifespren.particle.ModParticles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class LifesprenRenderer extends GeoEntityRenderer<LifesprenEntity> {
    public LifesprenRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LifesprenModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(LifesprenEntity instance) {
        return new ResourceLocation(LifesprenMod.MOD_ID, "textures/entity/lifespren_mob_texture.png");
    }

    @Override
    protected int getBlockLightLevel(LifesprenEntity pEntity, BlockPos pPos) {
        return 8;
    }

    @Override
    public RenderType getRenderType(LifesprenEntity entity, float partialTicks, PoseStack stack,
                                    @Nullable MultiBufferSource renderTypeBuffer,
                                    @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        //cool stuff can go in here
        stack.scale(0.5F, 0.5F, 0.5F);
        //spawn particle trail here so is in sync with entity position
        if (entity.level.isClientSide) {
            entity.level.addParticle(ModParticles.TRAIL_PARTICLES.get(), entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
        }
        return super.getRenderType(entity, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}
