package com.bavde1.lifespren.entity.client;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.entity.ModEntityTypes;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.bavde1.lifespren.particle.ModParticles;
import com.mojang.blaze3d.systems.RenderSystem;
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
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class LifesprenRenderer extends GeoEntityRenderer<LifesprenEntity> {
    ResourceLocation texture = new ResourceLocation(LifesprenMod.MOD_ID, "textures/entity/lifespren_mob_texture.png");

    public LifesprenRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LifesprenModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(LifesprenEntity instance) {
        return texture;
    }

    @Override
    protected int getBlockLightLevel(LifesprenEntity pEntity, BlockPos pPos) {
        return 10;
    }

    @Override
    public void render(LifesprenEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
    }

    @Override
    public RenderType getRenderType(LifesprenEntity entity, float partialTicks, PoseStack poseStack,
                                    @Nullable MultiBufferSource renderTypeBuffer,
                                    @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        //scale entity to half model size
        poseStack.scale(0.45F, 0.45F, 0.45F);
        //spawn particle trail here so is in sync with entity position
        if (entity.level.isClientSide) {
            entity.level.addParticle(ModParticles.TRAIL_PARTICLE.get(), entity.getX() + 0.005, entity.getY() + 0.08, entity.getZ(), 0, 0, 0);
        }

        //return super.getRenderType(entity, partialTicks, poseStack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
        return RenderType.entityTranslucent(textureLocation);
    }
}
