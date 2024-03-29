package com.bavde1.lifespren.entity.lifesprenEntities;

import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.sound.ModSounds;
import com.bavde1.lifespren.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import java.util.ArrayList;

/* todo:
    make proper despawning sound
 */

public class LifesprenEntity extends AmbientCreature implements GeoEntity {
    private final AnimatableInstanceCache factory = new SingletonAnimatableInstanceCache(this);
    private BlockPos targetPosition;

    public LifesprenEntity(EntityType<? extends AmbientCreature> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0D)
                .build();
    }

    public void tick() {
        //entity fly vertically
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.95D, 0.5D, 0.95D));
        //despawn entity
        int minAliveSec = 15;
        if (this.tickCount >= ((minAliveSec * 20) + (Math.random() * 4000))) {
            despawnLifespren();
        }
        //TrailTest.onTick(this);
        super.tick();
    }

    //basic AI
    protected void customServerAiStep() {
        int bX = this.getBlockX();
        int bY = this.getBlockY();
        int bZ = this.getBlockZ();

        //reset target pos
        if (this.targetPosition != null && (!this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level.getMinBuildHeight()) && Math.random() < 0.6) {
            this.targetPosition = null;
        }

        //new target pos
        if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D)) {
            //finds nearby blocks lifespren is attracted to
            int searchRange = 10;
            ArrayList<BlockPos> validBlockPos = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(bX - searchRange, bY - searchRange, bZ - searchRange, bX + searchRange, bY + searchRange, bZ + searchRange)) {
                Block block = level.getBlockState(pos.immutable()).getBlock();
                if (isLifesprenAttracting(block)) {
                    validBlockPos.add(pos.immutable());
                }
            }

            //selection of target pos
            if (!validBlockPos.isEmpty() && Math.random() < 0.85) { //85%
                //specific target pos
                //targetPos.offset(Math.random(), 0.7 + (Math.random() * 4), Math.random());
                this.targetPosition = validBlockPos.get((int) Math.floor(Math.random() * validBlockPos.size()));
            } else {
                //random target pos
                int range = 7;
                int rangeY = 4;
                double pX = this.getX() + (double) this.random.nextInt(range) - (double) this.random.nextInt(range);
                double pY = this.getY() + (double) this.random.nextInt(rangeY);
                double pZ = this.getZ() + (double) this.random.nextInt(range) - (double) this.random.nextInt(range);
                this.targetPosition = new BlockPos(pX, pY, pZ);
            }
        }

        //movement to target pos
        double dX = (double) this.targetPosition.getX() + Math.random() - this.getX();
        double dY = (double) this.targetPosition.getY() + (0.3 + Math.random()) - this.getY();
        double dZ = (double) this.targetPosition.getZ() + Math.random() - this.getZ();
        Vec3 vec3 = this.getDeltaMovement();
        double div = 2.8;
        double pX = ((Math.signum(dX) * 0.5D - vec3.x) * (double) 0.1F) / div;
        double pY = (Math.signum(dY) * (double) 0.7F - vec3.y) * (double) 0.1F;
        double pZ = ((Math.signum(dZ) * 0.5D - vec3.z) * (double) 0.1F) / div;
        Vec3 vec31 = vec3.add(pX, pY, pZ);
        this.setDeltaMovement(vec31);

        //rotation
        float f = (float) (Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI)) - 90.0F;
        float f1 = Mth.wrapDegrees(f - this.getYRot());
        this.zza = 0.5F;
        this.setYRot(this.getYRot() + f1);

        super.customServerAiStep();
    }

    private void despawnLifespren() {
        //sound
        if (!this.isSilent()) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_PLACE, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.5F + 1F, false);
        }
        if (!this.level.isClientSide) {
            //small particle explosion
            for (int i = 0; i < 10; ++i) {
                int div = 7;
                double sX = (this.random.nextFloat() * 2.0F - 1.0F) / div;
                double sY = (this.random.nextFloat() * 2.0F - 1.0F) / div;
                double sZ = (this.random.nextFloat() * 2.0F - 1.0F) / div;

                double pX = this.getX();
                double pY = this.getY();
                double pZ = this.getZ();

                if (Minecraft.getInstance().level != null) {
                    Minecraft.getInstance().particleEngine.createParticle(ModParticles.TRAIL_PARTICLE.get(), pX, pY, pZ, sX, sY + 0.2D, sZ);
                }
            }
            //despawn
            this.discard();
        }
    }

    private static boolean isLifesprenAttracting(Block block) {
        return block.defaultBlockState().is(ModTags.Blocks.LIFESPREN_ATTRACTING_BLOCKS);
    }

    //despawn on damage
    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource != DamageSource.CRAMMING) {
            despawnLifespren();
        }
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        //1/2
        return this.random.nextInt(2) != 0 ? null : ModSounds.LIFESPREN_CHIMES.get();
    }

    protected float getSoundVolume() {
        return 3F;
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pState) {
    }

    public boolean isPushable() {
        return false;
    }

    protected void doPush(Entity pEntity) {
    }

    protected void pushEntities() {
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height / 2.0F;
    }

    //how far away the entity should render
    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        double area = this.getBoundingBox().getSize();
        if (Double.isNaN(area)) {
            area = 1.0D;
        }

        //since lifespren has such a small hit box this needs to be raised
        area *= 150.0D; //default is 64.0D
        return pDistance < area * area;
    }

    //animation stuff
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.lifespren.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controller) {
        controller.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return factory;
    }
}
