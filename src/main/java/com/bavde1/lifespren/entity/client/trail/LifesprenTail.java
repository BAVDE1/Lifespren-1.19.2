
package com.bavde1.lifespren.entity.client.trail;

import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.bavde1.lifespren.util.vector.Vec3f;
import com.bavde1.lifespren.util.vector.VectorUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

public class LifesprenTail {
    public static final int MAX_LENGTH = 10;
    public static final float SPAWN_INTERVAL = 1;

    private final Minecraft mc;
    private LifesprenEntity trackedLifespren;
    private TrailNode[] nodes;
    private float spawnCooldown = 0;

    public LifesprenTail(LifesprenEntity lifespren) {
        this.mc = Minecraft.getInstance();
        this.trackedLifespren = lifespren;
        this.spawnCooldown = SPAWN_INTERVAL;
        this.nodes = new TrailNode[MAX_LENGTH];

        resetNodes();
    }

    public void onRenderTick(float partialTicks) {
        float s = spawnCooldown;
        spawnCooldown += partialTicks - s;
    }

    public void render(double x, double y, double z, float partialTicks, PoseStack stack) {
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

        renderNodes(partialTicks, stack);
    }

    public void resetNodes() {
        for (int i = 0; i < MAX_LENGTH; i++)
            this.nodes[i] = new TrailNode(trackedLifespren);
    }

    public void renderNodes(float partialTicks, PoseStack stack) {
        final Entity viewEntity = Minecraft.getInstance().getCameraEntity();

        if (viewEntity == null)
            return;
        Vec3 viewPos = new Vec3(viewEntity.xOld + (viewEntity.getX() - viewEntity.xOld) * partialTicks,
                viewEntity.yOld + (viewEntity.getY() - viewEntity.yOld) * partialTicks,
                viewEntity.zOld + (viewEntity.getZ() - viewEntity.zOld) * partialTicks);

        float r = 1;
        float g = 1;
        float b = 1;
        float a = 1F;
        stack.pushPose();
        //GlStateManager._clearColor(r, g, b, a);

        //GlStateManager.pushMatrix();
        GlStateManager._disableTexture();
        //GlStateManager.disableLighting();
        GlStateManager._disableCull();
        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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
            final Vec3f up0 = node0.up;
            final Vec3f right0 = node0.right;
            final Vec3f up1 = node1.up;
            final Vec3f right1 = node1.right;

            bufferBuilder.vertex(pos0.x + (-right0.x) * scale0, pos0.y + (-right0.y) * scale0, pos0.z + (-right0.z) * scale0)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(pos0.x + (right0.x) * scale0, pos0.y + (right0.y) * scale0, pos0.z + (right0.z) * scale0)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(pos1.x + (right1.x) * scale1, pos1.y + (right1.y) * scale1, pos1.z + (right1.z) * scale1)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(pos1.x + (-right1.x) * scale1, pos1.y + (-right1.y) * scale1, pos1.z + (-right1.z) * scale1)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();

            bufferBuilder.vertex(pos0.x + (-up0.x) * scale0, pos0.y + (-up0.y) * scale0, pos0.z + (-up0.z) * scale0)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(pos0.x + (up0.x) * scale0, pos0.y + (up0.y) * scale0, pos0.z + (up0.z) * scale0)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(pos1.x + (up1.x) * scale1, pos1.y + (up1.y) * scale1, pos1.z + (up1.z) * scale1)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(pos1.x + (-up1.x) * scale1, pos1.y + (-up1.y) * scale1, pos1.z + (-up1.z) * scale1)
                    .uv(0.0F, 0.15625F).color(r, g, b, a).endVertex();
        }
        tesselator.end();

        GlStateManager._enableCull();
        GlStateManager._enableTexture();
        stack.popPose();
        //GlStateManager.enableLighting();
        //GlStateManager.popMatrix();
    }

    public boolean shouldBeRemoved() {
        return mc.level == null || trackedLifespren.isDeadOrDying();
    }

    static class TrailNode {
        public double x;
        public double y;
        public double z;

        public final Vec3f up;
        public final Vec3f right;

        TrailNode(LifesprenEntity lifespren) {
            this.up = new Vec3f();
            this.right = new Vec3f();

            this.moveTo(lifespren);
        }

        public void moveTo(TrailNode trailNode) {
            this.x = trailNode.x;
            this.y = trailNode.y;
            this.z = trailNode.z;
            this.up.set(trailNode.up);
            this.right.set(trailNode.right);
        }

        public void moveTo(LifesprenEntity lifespren) {
            this.x = lifespren.getX();
            this.y = lifespren.getY();
            this.z = lifespren.getZ();

            final Vec3 forward = lifespren.getForward();
            final Vec3 up = Vec3.directionFromRotation(lifespren.yBodyRot + 90F, lifespren.rotA);

            this.up.set((float) -up.x, (float) -up.y, (float) up.z);

            VectorUtils.cross(
                    (float) -forward.x, (float) -forward.y, (float) forward.z,
                    this.up.x, this.up.y, this.up.z, this.right);
        }
    }


    /*public static final int MAX_LENGTH = 10;
    public static final float SPAWN_INTERVAL = 1;
    private float spawnCooldown = 0;

    private TrailNode[] nodes;

    public void renderTrail(LifesprenEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        Minecraft minecraft = Minecraft.getInstance();


    }

    static class TrailNode {
        public double x;
        public double y;
        public double z;

        public Vec3f up;
        public Vec3f right;

        TrailNode(LifesprenEntity lifespren) {
            this.up = new Vec3f();
            this.right = new Vec3f();

            this.moveTo(lifespren);
        }

        public void moveTo(TrailNode trailNode) {
            this.x = trailNode.x;
            this.y = trailNode.y;
            this.z = trailNode.z;

            this.up = trailNode.up;
            this.right = trailNode.right;
        }

        public void moveTo(LifesprenEntity lifespren) {
            this.x = lifespren.getX();
            this.y = lifespren.getY();
            this.z = lifespren.getZ();

            final Vec3 forward = lifespren.getForward();
            final Vec3 up = Vec3.directionFromRotation(lifespren.yBodyRot + 90F, lifespren.rotA);

            this.up.add((float) -up.x, (float) -up.y, (float) up.z);

            VectorUtils.cross((float) -forward.x, (float) -forward.y, (float) forward.z, this.up.x, this.up.y, this.up.z, this.right);
        }*/
}

    /*
    public static final int MAX_LENGTH = 10;
    public static final float SPAWN_INTERVAL = 1;

    private final Minecraft minecraft;
    private LifesprenEntity trackedLifespren;

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
        BufferBuilder vertexBuffer = BufferBuilder.;
        vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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

            vertexBuffer.vertex(pos0.x + (-right0.x) * scale0, pos0.y + (-right0.y) * scale0, pos0.z + (-right0.z) * scale0)..endVertex();
            vertexBuffer.vertex(pos0.x + (right0.x) * scale0, pos0.y + (right0.y) * scale0, pos0.z + (right0.z) * scale0).endVertex();
            vertexBuffer.vertex(pos1.x + (right1.x) * scale1, pos1.y + (right1.y) * scale1, pos1.z + (right1.z) * scale1).endVertex();
            vertexBuffer.vertex(pos1.x + (-right1.x) * scale1, pos1.y + (-right1.y) * scale1, pos1.z + (-right1.z) * scale1).endVertex();

            vertexBuffer.vertex(pos0.x + (-up0.x) * scale0, pos0.y + (-up0.y) * scale0, pos0.z + (-up0.z) * scale0).endVertex();
            vertexBuffer.vertex(pos0.x + (up0.x) * scale0, pos0.y + (up0.y) * scale0, pos0.z + (up0.z) * scale0).endVertex();
            vertexBuffer.vertex(pos1.x + (up1.x) * scale1, pos1.y + (up1.y) * scale1, pos1.z + (up1.z) * scale1).endVertex();
            vertexBuffer.vertex(pos1.x + (-up1.x) * scale1, pos1.y + (-up1.y) * scale1, pos1.z + (-up1.z) * scale1).endVertex();
        }
        tesselator.end();

        GlStateManager._enableCull();
        GlStateManager._enableTexture();
        //GlStateManager.enableLighting();
        //GlStateManager.popMatrix();
    }

    public boolean shouldBeRemoved() {
        return minecraft.level == null || trackedLifespren.isDeadOrDying();
    }

    static class TrailNode {
        public double x;
        public double y;
        public double z;

        public Vec3 up;
        public Vec3 right;

        TrailNode(LifesprenEntity lifespren) {
            this.up = new Vec3(0, 0, 0);
            this.right = new Vec3(0, 0, 0);

            this.moveTo(lifespren);
        }

        public void moveTo(TrailNode trailNode) {
            this.x = trailNode.x;
            this.y = trailNode.y;
            this.z = trailNode.z;
            this.up = trailNode.up;
            this.right = trailNode.right;
        }

        public void moveTo(LifesprenEntity lifespren) {
            this.x = lifespren.getX();
            this.y = lifespren.getY();
            this.z = lifespren.getZ();

            final Vec3 forward = lifespren.getForward();
            final Vec3 up = Vec3.directionFromRotation(lifespren.yBodyRot + 90F, lifespren.rotA);

            this.up.add((float) -up.x, (float) -up.y, (float) up.z);

            //VectorUtils.cross((float) -forward.x, (float) -forward.y, (float) forward.z, this.up.x, this.up.y, this.up.z, this.right);
        }
    }*/
