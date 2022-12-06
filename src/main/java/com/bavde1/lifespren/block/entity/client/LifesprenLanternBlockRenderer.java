package com.bavde1.lifespren.block.entity.client;

import com.bavde1.lifespren.block.entity.LifesprenLanternEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class LifesprenLanternBlockRenderer extends GeoBlockRenderer<LifesprenLanternEntity> {
    public LifesprenLanternBlockRenderer(BlockEntityRendererProvider.Context rendererProvider) {
        super(rendererProvider, new LifesprenLanternBlockModel());
    }

    @Override
    public RenderType getRenderType(LifesprenLanternEntity animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
