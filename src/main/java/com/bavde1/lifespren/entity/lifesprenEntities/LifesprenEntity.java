package com.bavde1.lifespren.entity.lifesprenEntities;

import com.bavde1.lifespren.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;
import java.util.stream.Stream;

//todo:
// particles? trail?
// slower movement
// make custom ambient sound
// fly near or around crops perhaps? might need goals
// despawn after 20 seconds or so (explode into particles? or shrink)

public class LifesprenEntity extends AmbientCreature implements IAnimatable, FlyingAnimal {
    private final AnimationFactory factory = new AnimationFactory(this);
    private BlockPos targetPosition;

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
        //makes entity fly vertically
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        super.tick();
    }

    //ai
    protected void customServerAiStep() {
        BlockPos blockpos = this.blockPosition();
        int X = this.getBlockX();
        int Y = this.getBlockY();
        int Z = this.getBlockZ();

        if (this.targetPosition != null && (!this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level.getMinBuildHeight())) {
            //reset target pos
            this.targetPosition = null;
        }

        if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D)) {
            int searchRange = 10;
            /*PoiManager poi = ((ServerLevel) LifesprenEntity.this.level).getPoiManager();
            Stream<PoiRecord> stream = poi.getInRange((typeHolder) -> {
                return typeHolder.is(ModTags.Blocks.LIFESPREN_ATTRACTING.location());
            }, blockpos, searchRange, PoiManager.Occupancy.ANY);

            List<BlockPos> list = stream.map(PoiRecord::getPos).toList();*/

            //gets blockpos withing range
            Stream<BlockPos> stream = BlockPos.betweenClosedStream(X - searchRange, Y - searchRange, Z - searchRange, X + searchRange, Y + searchRange, Z + searchRange);

            //filters blockpos to tag
            List<BlockPos> list = stream.filter(LifesprenEntity.this::lifesprenAttracting).toList();

            if (!list.isEmpty()) { //20%?
                int rand = (int) randomBetweenInt(list.size());
                this.targetPosition = list.get(rand);
                System.out.println(rand);
                System.out.println(list.get(rand));
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

        double d2 = (double) this.targetPosition.getX() + 0.5D - this.getX();
        double d0 = (double) this.targetPosition.getY() + 0.1D - this.getY();
        double d1 = (double) this.targetPosition.getZ() + 0.5D - this.getZ();

        Vec3 vec3 = this.getDeltaMovement();
        int div = 2;
        double pX = ((Math.signum(d2) * 0.5D - vec3.x) * (double) 0.1F) / div;
        double pY = (Math.signum(d0) * (double) 0.7F - vec3.y) * (double) 0.1F;
        double pZ = ((Math.signum(d1) * 0.5D - vec3.z) * (double) 0.1F) / div;
        Vec3 vec31 = vec3.add(pX, pY, pZ);
        this.setDeltaMovement(vec31);

        float f = (float) (Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI)) - 90.0F;
        float f1 = Mth.wrapDegrees(f - this.getYRot());
        this.zza = 0.5F;
        this.setYRot(this.getYRot() + f1);
        super.customServerAiStep();
    }

    private boolean lifesprenAttracting(BlockPos blockpos) {
        Block block = level.getBlockState(blockpos).getBlock();
        return block.defaultBlockState().is(ModTags.Blocks.LIFESPREN_ATTRACTING);
    }

    private static double randomBetweenInt(int max) {
        return Math.floor(Math.random() * max);
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
        return 3.5F;
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
