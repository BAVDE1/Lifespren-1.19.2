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
// fix animation
// fix hit box position
// fix despawn effects not working sometimes

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

    public void tick() {
        super.tick();
        //entity fly vertically
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.5D, 1.0D));
        //despawn entity
        int minAliveSec = 15;
        if (this.tickCount > minAliveSec * 20 && Math.random() < ((this.tickCount - minAliveSec * 20) / 5000F)) {
            despawnLifespren();
        }
    }

    //basic AI
    protected void customServerAiStep() {
        BlockPos blockpos = this.blockPosition();
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

            //setting target pos
            if (!validBlockPos.isEmpty() && Math.random() < 0.85) { //85%
                //specific target pos
                BlockPos targetPos = validBlockPos.get((int) Math.floor(Math.random() * validBlockPos.size()));
                targetPos.offset(Math.random(), 0.7 + (Math.random() * 4), Math.random());
                this.targetPosition = targetPos;
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

        //rotation
        float f = (float) (Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI)) - 90.0F;
        float f1 = Mth.wrapDegrees(f - this.getYRot());
        this.zza = 0.5F;
        this.setYRot(this.getYRot() + f1);

        super.customServerAiStep();
    }

    private void despawnLifespren() {
        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        if (this.level.isClientSide) {
            for (int i = 0; i < 16; ++i) {
                int div = 6;
                double sX = (this.random.nextFloat() * 2.0F - 1.0F) / div;
                double sY = (this.random.nextFloat() * 2.0F - 1.0F) / div;
                double sZ = (this.random.nextFloat() * 2.0F - 1.0F) / div;

                double pX = this.getX();
                double pY = this.getY();
                double pZ = this.getZ();

                this.level.addParticle(ModParticles.TRAIL_PARTICLES.get(), false, pX, pY, pZ, sX, sY + 0.2D, sZ);
            }
        }
        this.discard();
    }

    private static boolean isLifesprenAttracting(Block block) {
        return block.defaultBlockState().is(ModTags.Blocks.LIFESPREN_ATTRACTING_BLOCKS);
    }

    //despawn on damage
    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        despawnLifespren();
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
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
        event.getController().setAnimation(new AnimationBuilder().addAnimation("lifespren.animation", true));
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
