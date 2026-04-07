package com.nuclearpark.item;

import com.nuclearpark.NuclearParkMod;
import com.nuclearpark.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class NuclearBombItem extends Item {
    private static final int EXPLOSION_RADIUS = 50;
    private static final int FIRE_RADIUS = 40;
    private static final int RADIATION_RADIUS = 80;

    public NuclearBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide) {
            triggerNuclearExplosion(player.level(), entity.blockPosition(), player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return true;
    }

    private void triggerNuclearExplosion(Level level, BlockPos center, Player owner) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Play nuclear explosion sound
        level.playSound(null, center, ModSounds.NUCLEAR_EXPLOSION.get(), 
            SoundSource.BLOCKS, 100.0F, 0.8F);

        // Create massive explosion effect
        for (int i = 0; i < 500; i++) {
            double x = center.getX() + (serverLevel.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            double y = center.getY() + serverLevel.random.nextDouble() * 30;
            double z = center.getZ() + (serverLevel.random.nextDouble() - 0.5) * EXPLOSION_RADIUS * 2;
            
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
        }

        // Mushroom cloud particles
        for (int i = 0; i < 200; i++) {
            double angle = serverLevel.random.nextDouble() * Math.PI * 2;
            double radius = serverLevel.random.nextDouble() * 10;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            
            for (int y = 0; y < 50; y++) {
                serverLevel.sendParticles(ParticleTypes.FLAME, 
                    x, center.getY() + y, z, 5, 0.5, 0.5, 0.5, 0.02);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, 
                    x, center.getY() + y, z, 3, 0.5, 0.5, 0.5, 0.05);
            }
        }

        // Destroy blocks in radius
        for (int x = -EXPLOSION_RADIUS; x <= EXPLOSION_RADIUS; x++) {
            for (int y = -EXPLOSION_RADIUS; y <= EXPLOSION_RADIUS; y++) {
                for (int z = -EXPLOSION_RADIUS; z <= EXPLOSION_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    double distance = Math.sqrt(center.distSqr(pos));
                    
                    if (distance <= EXPLOSION_RADIUS && !level.getBlockState(pos).isAir()) {
                        // Bedrock and other unbreakable blocks are spared
                        if (level.getBlockState(pos).getBlock() != Blocks.BEDROCK &&
                            level.getBlockState(pos).getBlock() != Blocks.END_PORTAL &&
                            level.getBlockState(pos).getBlock() != Blocks.END_GATEWAY) {
                            level.destroyBlock(pos, false);
                        }
                    }
                }
            }
        }

        // Set fires in radius
        for (int x = -FIRE_RADIUS; x <= FIRE_RADIUS; x++) {
            for (int z = -FIRE_RADIUS; z <= FIRE_RADIUS; z++) {
                BlockPos pos = center.offset(x, 0, z);
                double distance = Math.sqrt(center.distSqr(pos));
                
                if (distance <= FIRE_RADIUS && serverLevel.random.nextFloat() < 0.3F) {
                    // Find ground level
                    for (int y = 0; y < 20; y++) {
                        BlockPos checkPos = pos.above(y);
                        if (level.getBlockState(checkPos).isAir() && 
                            !level.getBlockState(checkPos.below()).isAir()) {
                            level.setBlockAndUpdate(checkPos, Blocks.FIRE.defaultBlockState());
                            break;
                        }
                    }
                }
            }
        }

        // Kill/damage entities in radius
        AABB blastZone = new AABB(
            center.getX() - RADIATION_RADIUS, center.getY() - RADIATION_RADIUS, center.getZ() - RADIATION_RADIUS,
            center.getX() + RADIATION_RADIUS, center.getY() + RADIATION_RADIUS, center.getZ() + RADIATION_RADIUS
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, blastZone);
        for (LivingEntity entity : entities) {
            double distance = entity.position().distanceTo(Vec3.atCenterOf(center));
            
            if (distance <= EXPLOSION_RADIUS * 0.5) {
                // Instant death in core
                entity.kill();
            } else if (distance <= EXPLOSION_RADIUS) {
                // Heavy damage in explosion zone
                entity.hurt(level.damageSources().explosion(null), 100.0F);
            } else if (distance <= RADIATION_RADIUS) {
                // Radiation damage
                entity.hurt(level.damageSources().magic(), 20.0F);
                entity.setSecondsOnFire(10);
            }
        }

        NuclearParkMod.LOGGER.info("Nuclear explosion triggered at {}", center);
    }
}
