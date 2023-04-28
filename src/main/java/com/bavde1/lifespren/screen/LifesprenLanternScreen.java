package com.bavde1.lifespren.screen;

import com.bavde1.lifespren.LifesprenMod;
import com.bavde1.lifespren.util.ModTags;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LifesprenLanternScreen extends AbstractContainerScreen<LifesprenLanternMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(LifesprenMod.MOD_ID, "textures/gui/lifespren_lantern_gui_default.png");

    public LifesprenLanternScreen(LifesprenLanternMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.titleLabelY = -22;
        this.inventoryLabelY = 101;
        this.imageWidth = 176;
        this.imageHeight = 222;
        //top corner of gui image
        int x = (width - this.imageWidth) / 2;
        int y = (height - this.imageHeight) / 2;

        this.blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);
    }

    //for when user hovers over icon in GUI
    @Override
    protected void renderTooltip(PoseStack poseStack, int mX, int mY) {
        super.renderTooltip(poseStack, mX, mY);
        int x = mX - ((width - this.imageWidth) / 2);
        int y = mY - ((height - this.imageHeight) / 2);
        if (x > 10 && x < 46) {
            if (y > 22 && y < 58) {
                LifesprenLanternScreen.this.renderTooltip(poseStack, Component.literal("Grows nearby crops"), mX, mY);
            }
        }
    }

    //extra info display
    @Override
    protected void renderLabels(PoseStack poseStack, int mX, int mY) {
        RenderSystem.disableBlend();
        super.renderLabels(poseStack, mX, mY);

        Component countComponent = null;
        Component hRangeComponent = null;
        Component vRangeComponent = null;
        if (this.menu.getSlot(0).hasItem()) {
            if (this.menu.slots.get(0).getItem().is(Items.REDSTONE)) {
                int count = this.menu.getNearbyCropsCount();
                countComponent = Component.translatable("Nearby: " + count);

                int hRange = this.menu.getHRange();
                hRangeComponent = Component.translatable("↔ Range: " + hRange);

                int vRange = this.menu.getVRange();
                vRangeComponent = Component.translatable("↕ Range: " + vRange);
            }
        }

        if (countComponent != null) {
            this.font.draw(poseStack, countComponent, 115, -8, 1325400064);
        }
        if (hRangeComponent != null) {
            this.font.draw(poseStack, hRangeComponent, 115, 2, 1325400064);
        }
        if (vRangeComponent != null) {
            this.font.draw(poseStack, vRangeComponent, 115, 12, 1325400064);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
