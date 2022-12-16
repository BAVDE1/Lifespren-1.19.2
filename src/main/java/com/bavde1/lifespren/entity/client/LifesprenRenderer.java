package com.bavde1.lifespren.entity.client;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.entity.client.trail.TrailTest;
import com.bavde1.lifespren.entity.client.trail.TrailTestManager;
import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import java.util.ArrayList;

public class LifesprenRenderer extends GeoEntityRenderer<LifesprenEntity> {
    ResourceLocation texture = new ResourceLocation(LifesprenMod.MOD_ID, "textures/entity/lifespren_mob_texture.png");
    private static final ArrayList<BlockPos> previousPositions = new ArrayList<>();

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
    public void render(LifesprenEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);

        //LifesprenTrailManager.renderTrail(entity, entity.getX(), entity.getY(), entity.getZ(), partialTicks, poseStack);
        //render here
    }

    @Override
    public RenderType getRenderType(LifesprenEntity entity, float partialTicks, PoseStack poseStack,
                                    @Nullable MultiBufferSource renderTypeBuffer,
                                    @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        //scale entity to half model size
        poseStack.scale(0.5F, 0.5F, 0.5F);
        //spawn particle trail here so is in sync with entity position
        if (entity.level.isClientSide) {
            TrailTestManager.renderTrail(entity);
            //entity.level.addParticle(ModParticles.TRAIL_PARTICLE.get(), entity.getX() + 0.005, entity.getY() + 0.08, entity.getZ(), 0, 0, 0);
        }

        //return super.getRenderType(entity, partialTicks, poseStack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
        return RenderType.entityTranslucent(textureLocation);
    }
}
