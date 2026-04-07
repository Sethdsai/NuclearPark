package com.nuclearpark.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ParksEntity extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> DATA_ANGRY = 
        SynchedEntityData.defineId(ParksEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_KILL_COUNT = 
        SynchedEntityData.defineId(ParksEntity.class, EntityDataSerializers.INT);

    public ParksEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 500.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.4D)
            .add(Attributes.ATTACK_DAMAGE, 1000.0D)
            .add(Attributes.FOLLOW_RANGE, 64.0D)
            .add(Attributes.ARMOR, 50.0D)
            .add(Attributes.ARMOR_TOUGHNESS, 25.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.6F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ANGRY, false);
        this.entityData.define(DATA_KILL_COUNT, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Angry", this.entityData.get(DATA_ANGRY));
        tag.putInt("KillCount", this.entityData.get(DATA_KILL_COUNT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_ANGRY, tag.getBoolean("Angry"));
        this.entityData.set(DATA_KILL_COUNT, tag.getInt("KillCount"));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (itemstack.is(Items.BONE) && !this.isTame()) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte)7);
            } else {
                this.level().broadcastEntityEvent(this, (byte)6);
            }
            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (itemstack.is(Items.ROTTEN_FLESH)) {
                if (this.getHealth() < this.getMaxHealth()) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.heal(20.0F);
                    return InteractionResult.SUCCESS;
                }
            } else {
                this.setOrderedToSit(!this.isOrderedToSit());
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        
        if (flag && target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            
            // Parks kills ANY mob instantly
            livingTarget.setHealth(0);
            
            // Increment kill count
            int kills = this.entityData.get(DATA_KILL_COUNT) + 1;
            this.entityData.set(DATA_KILL_COUNT, kills);
            
            // Spawn death particles
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL, 
                    target.getX(), target.getY() + 1, target.getZ(), 
                    50, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        return flag;
    }

    public int getKillCount() {
        return this.entityData.get(DATA_KILL_COUNT);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.isTame() && this.getOwner() != null && target == this.getOwner()) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.ROTTEN_FLESH);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null; // Parks cannot breed
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WOLF_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    public void tick() {
        super.tick();
        
        // Ambient particle effect
        if (this.level().isClientSide && this.random.nextInt(20) == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, 
                this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                this.getY() + this.random.nextDouble() * this.getBbHeight(),
                this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                0.0, 0.05, 0.0);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // Parks is immune to most damage
        if (source.isExplosion() || source.isFire() || source.isMagic()) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }
}
