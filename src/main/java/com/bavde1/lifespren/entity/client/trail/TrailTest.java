package com.bavde1.lifespren.entity.client.trail;

import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.util.vector.Vec3f;
import com.bavde1.lifespren.util.vector.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class TrailTest {
    public static final int MAX_LENGTH = 10;

    private LifesprenEntity trackedLifespren;
    private TrailNode[] nodes;

    public TrailTest(LifesprenEntity lifespren) {
        this.trackedLifespren = lifespren;
        this.nodes = new TrailNode[MAX_LENGTH];

        resetNodes();
    }

    public void render() {
        for (int i = 0; i < MAX_LENGTH; i++) {
            double xO = 0.005;
            double yO = 0.08;
            TrailNode n = nodes[i];
            if (trackedLifespren.level.isClientSide) {
                trackedLifespren.level.addParticle(ModParticles.TRAIL_PARTICLE.get(), n.x + xO, n.y + yO, n.z, 0, 0, 0);
            }
        }
        progressArray();
    }

    public void resetNodes() {
        for (int i = 0; i < MAX_LENGTH; i++)
            this.nodes[i] = new TrailNode(trackedLifespren);
    }

    private void progressArray() {
        nodes[9] = nodes[8];
        nodes[8] = nodes[7];
        nodes[7] = nodes[6];
        nodes[6] = nodes[5];
        nodes[5] = nodes[4];
        nodes[4] = nodes[3];
        nodes[3] = nodes[2];
        nodes[2] = nodes[1];
        nodes[1] = nodes[0];
        nodes[0] = new TrailNode(trackedLifespren);
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
}
