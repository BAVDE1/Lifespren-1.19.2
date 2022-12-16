package com.bavde1.lifespren.entity.client.trail;

import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.HashMap;

public class TrailTestManager {
    private static HashMap<LifesprenEntity, TrailTest> trailMap = new HashMap<>();
    public static long time, lastTime;

    static {
        time = System.nanoTime() / 1000;
        lastTime = System.nanoTime() / 1000;
    }

    public static TrailTest getOrMake(LifesprenEntity lifespren) {
        TrailTest trail;
        if (!trailMap.containsKey(lifespren)) {
            trail = new TrailTest(lifespren);
            trailMap.put(lifespren, trail);
        } else {
            trail = trailMap.get(lifespren);
        }

        return trail;
    }

    public static void renderTrail(LifesprenEntity entity) {
        getOrMake(entity).render();
    }
}
