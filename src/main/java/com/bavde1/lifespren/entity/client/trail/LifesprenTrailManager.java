package com.bavde1.lifespren.entity.client.trail;

import com.bavde1.lifespren.entity.lifesprenEntities.LifesprenEntity;

import java.util.HashMap;

public class LifesprenTrailManager {
    private static HashMap<LifesprenEntity, LifesprenTail> trailMap = new HashMap<>();
    public static long time, lastTime;

    static {
        time = System.nanoTime() / 1000;
        lastTime = System.nanoTime() / 1000;
    }

    public static LifesprenTail getOrMake(LifesprenEntity lifespren) {
        LifesprenTail trail;
        if (!trailMap.containsKey(lifespren)) {
            trail = new LifesprenTail(lifespren);
            trailMap.put(lifespren, trail);
        } else {
            trail = trailMap.get(lifespren);
        }

        return trail;
    }

    public static void renderTrail(LifesprenEntity entity, double x, double y, double z, float partialTicks) {
        getOrMake(entity).render(x, y, z, partialTicks);
    }

    public static void cleanup() {
        trailMap.entrySet().removeIf(e -> e.getValue().shouldBeRemoved());
    }

    public static void onRenderTick() {
        for (final LifesprenTail trail : trailMap.values()) {
            trail.onRenderTick();
        }

        cleanup();
    }
}
