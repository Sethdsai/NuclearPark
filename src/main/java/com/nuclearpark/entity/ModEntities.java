package com.nuclearpark.entity;

import com.nuclearpark.NuclearParkMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NuclearParkMod.MOD_ID);

    public static final RegistryObject<EntityType<ParksEntity>> PARKS = ENTITIES.register("parks",
        () -> EntityType.Builder.of(ParksEntity::new, MobCategory.CREATURE)
            .sized(0.9F, 1.2F)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(NuclearParkMod.MOD_ID + ":parks"));
}
