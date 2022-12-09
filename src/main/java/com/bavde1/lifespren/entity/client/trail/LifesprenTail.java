package com.bavde1.lifespren.entity.client.trail;

import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.util.VectorUtils;

public class LifesprenTail {
    public static final int MAX_LENGTH = 10;
    public static final float SPAWN_INTERVAL = 1;

    private final Minecraft minecraft;
    private LifesprenEntity trackedLifespren;
    private TrailNode[] nodes;
    private float spawnCooldown = 0;

    public LifesprenTail(LifesprenEntity lifespren) {
        this.minecraft = Minecraft.getInstance();
        this.trackedLifespren = lifespren;
        this.spawnCooldown = SPAWN_INTERVAL;
        this.nodes = new TrailNode[MAX_LENGTH];

        resetNodes();
    }

    public void onRenderTick() {
        //spawnCooldown += DataUpdateHandler.ticksPerFrame;
    }

    public void render(double x, double y, double z, float partialTicks) {
        if (this.spawnCooldown > 40) {
            this.spawnCooldown = 0;
            resetNodes();
        }

        while (this.spawnCooldown >= SPAWN_INTERVAL) {
            for (int i = MAX_LENGTH - 1; i > 0; i--) {
                nodes[i].moveTo(nodes[i - 1]);
            }
            nodes[0].moveTo(trackedLifespren);
            this.spawnCooldown -= SPAWN_INTERVAL;
        }

        renderNodes(partialTicks);
    }

    public void resetNodes() {
        for (int i = 0; i < MAX_LENGTH; i++)
            this.nodes[i] = new TrailNode(trackedLifespren);
    }

    public void renderNodes(float partialTicks) {
        final Entity viewEntity = Minecraft.getInstance().getCameraEntity();

        if (viewEntity == null) {
            return;
        }
        Vec3 viewPos = new Vec3(viewEntity.xOld + (viewEntity.getX() - viewEntity.xOld) * partialTicks,
                viewEntity.yOld + (viewEntity.getY() - viewEntity.yOld) * partialTicks,
                viewEntity.zOld + (viewEntity.getZ() - viewEntity.zOld) * partialTicks);

        float r = 1;
        float g = 1;
        float b = 1;
        float a = 0.5F;
        RenderSystem.setShaderColor(r, g, b, a);

        //GlStateManager.pushMatrix();
        GlStateManager._disableTexture();
        //GlStateManager.disableLighting();
        GlStateManager._disableCull();
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexbuffer = tesselator.getBuilder();
        vertexbuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (int i = 1; i < MAX_LENGTH; i++) {
            TrailNode node0 = nodes[i - 1];
            TrailNode node1 = nodes[i];

            Vec3 pos0 = new Vec3(node0.x - viewPos.x, node0.y - viewPos.y, node0.z - viewPos.z);
            Vec3 pos1 = new Vec3(node1.x - viewPos.x, node1.y - viewPos.y, node1.z - viewPos.z);
            float scale0 = ((float) (MAX_LENGTH - i)) / MAX_LENGTH * .1F;
            float scale1 = ((float) MAX_LENGTH - i - 1.0f) / MAX_LENGTH * .1F;
            if (i == 1) {
                scale1 = 0;
            }
            final Vec3 up0 = node0.up;
            final Vec3 right0 = node0.right;
            final Vec3 up1 = node1.up;
            final Vec3 right1 = node1.right;

            vertexbuffer
                    .pos(pos0.x + (-right0.x) * scale0, pos0.y + (-right0.y) * scale0, pos0.z + (-right0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos0.x + (right0.x) * scale0, pos0.y + (right0.y) * scale0, pos0.z + (right0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos1.x + (right1.x) * scale1, pos1.y + (right1.y) * scale1, pos1.z + (right1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer
                    .pos(pos1.x + (-right1.x) * scale1, pos1.y + (-right1.y) * scale1, pos1.z + (-right1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();

            vertexbuffer.pos(pos0.x + (-up0.x) * scale0, pos0.y + (-up0.y) * scale0, pos0.z + (-up0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos0.x + (up0.x) * scale0, pos0.y + (up0.y) * scale0, pos0.z + (up0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos1.x + (up1.x) * scale1, pos1.y + (up1.y) * scale1, pos1.z + (up1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos1.x + (-up1.x) * scale1, pos1.y + (-up1.y) * scale1, pos1.z + (-up1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();
        }
        tesselator.end();

        GlStateManager._enableCull();
        GlStateManager._enableTexture();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    /**
     * Used for debugging.
     *
     * @param x
     * @param y
     * @param z
     */
    public void renderAxis(double x, double y, double z) {
        Vec3 forward = trackedLifespren.getForward();
        forward = new Vec3(-forward.x, -forward.y, forward.z);
        Vec3 up = Vec3.directionFromRotation(trackedLifespren.yBodyRot + 90.0f, trackedLifespren.rotA);
        up = new Vec3(-up.x, -up.y, up.z);
        Vec3 right = forward.cross(up);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.depthFunc(GL11.GL_ALWAYS);
        GlStateManager.translate(x, y, z);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexbuffer = tesselator.getBuilder();
        GlStateManager.color(1, 0, 0, 1);
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(right.x, right.y, right.z).tex(0.15625D, 0.15625D).endVertex();
        tesselator.draw();
        GlStateManager.color(0, 1, 0, 1);
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(up.x, up.y, up.z).tex(0.15625D, 0.15625D).endVertex();
        tesselator.draw();
        GlStateManager.color(0, 0, 1, 1);
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(forward.x, forward.y, forward.z).tex(0.15625D, 0.15625D).endVertex();
        tesselator.draw();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public boolean shouldBeRemoved() {
        return minecraft.level == null || trackedLifespren.isDeadOrDying();
    }

    static class TrailNode {
        public double x;
        public double y;
        public double z;

        public final Vec3 up;
        public final Vec3 right;

        TrailNode(LifesprenEntity lifespren) {
            this.up = new Vec3();
            this.right = new Vec3();

            this.moveTo(lifespren);
        }

        public void moveTo(TrailNode trailNode) {
            this.x = trailNode.x;
            this.y = trailNode.y;
            this.z = trailNode.z;
            this.up.add(trailNode.up);
            this.right.add(trailNode.right);
        }

        public void moveTo(LifesprenEntity lifespren) {
            this.x = lifespren.getX();
            this.y = lifespren.getY();
            this.z = lifespren.getZ();

            final Vec3 forward = lifespren.getForward();
            final Vec3 up = Vec3.directionFromRotation(lifespren.yBodyRot + 90F, lifespren.rotA);

            this.up.add((float) -up.x, (float) -up.y, (float) up.z);

            VectorUtils.cross(
                    (float) -forward.x, (float) -forward.y, (float) forward.z,
                    this.up.x, this.up.y, this.up.z, this.right);
        }
    }
}
