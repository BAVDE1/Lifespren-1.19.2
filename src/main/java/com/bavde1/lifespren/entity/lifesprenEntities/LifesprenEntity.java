package com.bavde1.lifespren.entity.lifesprenEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;

//todo: make spren 2x2 size, slower movement, increase ambient sound & fly near or around crops perhaps?

public class LifesprenEntity extends AmbientCreature implements IAnimatable, FlyingAnimal {
    private final AnimationFactory factory = new AnimationFactory(this);
    private BlockPos targetPosition;

    public LifesprenEntity(EntityType<? extends AmbientCreature> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0D)
                .add(Attributes.FLYING_SPEED, 0.1D)
                .build();
    }

    protected void registerGoals() {
        super.registerGoals();
    }

    public void tick() {
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        super.tick();
    }

    //ai
    protected void customServerAiStep() {
        BlockPos blockpos = this.blockPosition();
        BlockPos blockPosBelow = blockpos.below();

        Block blockUnder = level.getBlockState(blockPosBelow.immutable()).getBlock();
        if (blockUnder == Blocks.WHEAT) {
            //lol
        }

        //stolen bat ai lol
        if (this.targetPosition != null && (!this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level.getMinBuildHeight())) {
            this.targetPosition = null;
        }

        if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D)) {
            this.targetPosition = new BlockPos(this.getX() + (double) this.random.nextInt(7) - (double) this.random.nextInt(7), this.getY() + (double) this.random.nextInt(6) - 2.0D, this.getZ() + (double) this.random.nextInt(7) - (double) this.random.nextInt(7));
        }

        double d2 = (double) this.targetPosition.getX() + 0.5D - this.getX();
        double d0 = (double) this.targetPosition.getY() + 0.1D - this.getY();
        double d1 = (double) this.targetPosition.getZ() + 0.5D - this.getZ();
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec31 = vec3.add((Math.signum(d2) * 0.5D - vec3.x) * (double) 0.1F, (Math.signum(d0) * (double) 0.7F - vec3.y) * (double) 0.1F, (Math.signum(d1) * 0.5D - vec3.z) * (double) 0.1F);
        this.setDeltaMovement(vec31);
        float f = (float) (Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI)) - 90.0F;
        float f1 = Mth.wrapDegrees(f - this.getYRot());
        this.zza = 0.5F;
        this.setYRot(this.getYRot() + f1);

        super.customServerAiStep();
    }

    //cancels attacks
    public boolean skipAttackInteraction(Entity pEntity) {
        return true;
    }

    //sounds
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    protected float getSoundVolume() {
        return 1.0F;
    }

    //always flying
    public boolean isFlying() {
        return true;
    }

    //cancel all pushing
    public boolean isPushable() {
        return false;
    }
    protected void doPush(Entity pEntity) {
    }
    protected void pushEntities() {
    }

    //immune to fall damage
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    //determines if the entity ignores block triggers like pressure plates
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    //cannot be leashed
    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height / 2.0F;
    }

    //animation stuff
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.lifespren.idle", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
