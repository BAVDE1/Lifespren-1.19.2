package com.bavde1.lifespren.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LineParticle extends TextureSheetParticle {
    LineParticle(ClientLevel pLevel, double pX, double pY, double pZ, double sX, double sY, double sZ) {
        super(pLevel, pX, pY, pZ, sX, sY, sZ);
        float f = this.random.nextFloat() * 0.1F + 0.2F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.5F;
        this.xd *= 0.02F;
        this.yd *= 0.02F;
        this.zd *= 0.02F;
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * (1.0F - f * f * 0.5F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.99D;
            this.yd *= 0.99D;
            this.zd *= 0.99D;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LineProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public LineProvider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double pX, double pY, double pZ, double sX, double sY, double sZ) {
            LineParticle lineParticle = new LineParticle(level, pX, pY, pZ, sX, sY, sZ);
            lineParticle.pickSprite(this.sprite);
            lineParticle.setColor(1.0F, 1.0F, 1.0F);
            lineParticle.setLifetime((int)(20.0D / (Math.random() * 0.8D + 0.2D)));
            return lineParticle;
        }
    }
}
