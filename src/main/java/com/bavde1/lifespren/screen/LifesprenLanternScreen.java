package com.bavde1.lifespren.screen;

import com.bavde1.lifespren.LifesprenMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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

        //crop
        if (x > 9 && x < 45 && y > 21 && y < 57) {
            LifesprenLanternScreen.this.renderTooltip(poseStack, Component.translatable("screen.lifespren.lifespren_lantern.crops_tooltip"), mX, mY);
        }

        //icons
        if (this.menu.hasRedstone()) {
            if (x > 115 && x < 123 && y > 35 && y < 43) {
                LifesprenLanternScreen.this.renderTooltip(poseStack, Component.translatable("screen.lifespren.lifespren_lantern.nearby_blocks_tooltip"), mX, mY);
            }
            if (x > 112 && x < 126 && y > 50 && y < 58) {
                LifesprenLanternScreen.this.renderTooltip(poseStack, Component.translatable("screen.lifespren.lifespren_lantern.range_tooltip"), mX, mY);
            }
            if (x > 113 && x < 125 && y > 65 && y < 77) {
                LifesprenLanternScreen.this.renderTooltip(poseStack, Component.translatable("screen.lifespren.lifespren_lantern.cooldown_tooltip"), mX, mY);
            }
            if (x > 113 && x < 125 && y > 84 && y < 96) {
                LifesprenLanternScreen.this.renderTooltip(poseStack, Component.translatable("screen.lifespren.lifespren_lantern.special_chance_tooltip"), mX, mY);
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
        Component cooldownComponent = null;
        Component specialComponent = null;
        if (this.menu.hasRedstone()) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, TEXTURE);

            //icons
            this.blit(poseStack, 114, 9, imageWidth, 0, 11, 58);

            //changeable values
            int count = this.menu.getNearbyCropsCount();
            countComponent = Component.translatable("- " + count);

            int hRange = this.menu.getHRange();
            hRangeComponent = Component.translatable("- " + hRange);

            cooldownComponent = Component.translatable("- ");

            specialComponent = Component.translatable("- ");
        }

        int x = 130;
        int colour = 1325400064;
        if (countComponent != null) {
            this.font.draw(poseStack, countComponent, x, 8, colour);
        }
        if (hRangeComponent != null) {
            this.font.draw(poseStack, hRangeComponent, x, 23, colour);
        }
        if (cooldownComponent != null) {
            this.font.draw(poseStack, cooldownComponent, x, 40, colour);
        }
        if (specialComponent != null) {
            this.font.draw(poseStack, specialComponent, x, 59, colour);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
