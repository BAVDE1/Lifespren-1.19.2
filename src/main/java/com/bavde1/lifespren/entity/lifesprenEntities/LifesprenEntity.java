package com.bavde1.lifespren.entity.lifesprenEntities;

import com.bavde1.lifespren.particle.ModParticles;
import com.bavde1.lifespren.sound.ModSounds;
import com.bavde1.lifespren.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

//todo:
// particles? trail?
// despawn after 20 seconds or so (explode into particles? or shrink)

public class LifesprenEntity extends AmbientCreature implements IAnimatable, FlyingAnimal {
    private final AnimationFactory factory = new AnimationFactory(this);
    private BlockPos targetPosition;
    private Minecraft minecraft;

    public LifesprenEntity(EntityType<? extends AmbientCreature> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0D)
                .build();
    }

    protected void registerGoals() {
        super.registerGoals();
    }

    public void tick() {
        super.tick();
        //entity fly vertically
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.5D, 1.0D));
        //trail
        if (this.level.isClientSide) {
            this.level.addParticle(ModParticles.TRAIL_PARTICLES.get(), this.getX(-0.5), this.getY(), this.getZ(-0.5), 0, 0, 0);
        }
        //despawn rules
        if (this.tickCount > 300 && Math.random() < ((this.tickCount - 300F) / 5000F)) {
            despawnLifespren();
        }
    }

    //basic AI
    protected void customServerAiStep() {
        BlockPos blockpos = this.blockPosition();
        int X = this.getBlockX();
        int Y = this.getBlockY();
        int Z = this.getBlockZ();

        //heal every 10 ticks
        if (!this.level.isClientSide && this.isAlive() && this.tickCount % 10 == 0) {
            this.heal(1.0F);
        }

        if (this.targetPosition != null && (!this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level.getMinBuildHeight()) && Math.random() < 0.6) {
            //reset target pos
            this.targetPosition = null;
        }

        if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D)) {
            //finds nearby blocks lifespren is attracted to
            int searchRange = 10;
            ArrayList<BlockPos> validBlockPos = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(X - searchRange, Y - searchRange, Z - searchRange, X + searchRange, Y + searchRange, Z + searchRange)) {
                Block block = level.getBlockState(pos.immutable()).getBlock();
                if (isLifesprenAttracting(block)) {
                    validBlockPos.add(pos.immutable());
                }
            }

            //setting target pos
            if (!validBlockPos.isEmpty() && Math.random() < 0.85) { //85%
                BlockPos targetPos = validBlockPos.get((int) Math.floor(Math.random() * validBlockPos.size()));
                targetPos.offset(Math.random(), 0.3 + (Math.random() * 4), Math.random());
                this.targetPosition = targetPos;
            } else {
                //gets random blockpos withing 7 blocks (bat AI)
                int range = 7;
                int rangeY = 4;
                double pX = this.getX() + (double) this.random.nextInt(range) - (double) this.random.nextInt(range);
                double pY = this.getY() + (double) this.random.nextInt(rangeY);
                double pZ = this.getZ() + (double) this.random.nextInt(range) - (double) this.random.nextInt(range);
                this.targetPosition = new BlockPos(pX, pY, pZ);
            }
        }

        double dX = (double) this.targetPosition.getX() + 0.5D - this.getX();
        double dY = (double) this.targetPosition.getY() + 0.1D - this.getY();
        double dZ = (double) this.targetPosition.getZ() + 0.5D - this.getZ();

        Vec3 vec3 = this.getDeltaMovement();
        double div = 2.8;
        double pX = ((Math.signum(dX) * 0.5D - vec3.x) * (double) 0.1F) / div;
        double pY = (Math.signum(dY) * (double) 0.7F - vec3.y) * (double) 0.1F;
        double pZ = ((Math.signum(dZ) * 0.5D - vec3.z) * (double) 0.1F) / div;
        Vec3 vec31 = vec3.add(pX, pY, pZ);
        this.setDeltaMovement(vec31);

        float f = (float) (Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI)) - 90.0F;
        float f1 = Mth.wrapDegrees(f - this.getYRot());
        this.zza = 0.5F;
        this.setYRot(this.getYRot() + f1);
        super.customServerAiStep();
    }

    private static boolean isLifesprenAttracting(Block block) {
        return block.defaultBlockState().is(ModTags.Blocks.LIFESPREN_ATTRACTING_BLOCKS);
    }

    private void despawnLifespren() {
        this.discard();
        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        if (this.level.isClientSide) {
            for (int i = 0; i < 10; ++i) {
                double sX = (this.random.nextFloat() * 2.0F - 1.0F) / 5;
                double sY = (this.random.nextFloat() * 2.0F - 1.0F) / 5;
                double sZ = (this.random.nextFloat() * 2.0F - 1.0F) / 5;

                double pX = this.getX();
                double pY = this.getY();
                double pZ = this.getZ();

                this.level.addParticle(ModParticles.TRAIL_PARTICLES.get(), false, pX, pY, pZ, sX, sY + 0.2D, sZ);
            }
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    //cancels attacks
    public boolean skipAttackInteraction(Entity pEntity) {
        return true;
    }

    //sounds
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
