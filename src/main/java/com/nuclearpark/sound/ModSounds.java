package com.nuclearpark.sound;

import com.nuclearpark.NuclearParkMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = 
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NuclearParkMod.MOD_ID);

    public static final RegistryObject<SoundEvent> NUCLEAR_EXPLOSION = SOUNDS.register("nuclear_explosion",
        () -> SoundEvent.createVariableRangeEvent(
            new ResourceLocation(NuclearParkMod.MOD_ID, "nuclear_explosion")));

    public static final RegistryObject<SoundEvent> PARKS_GROWL = SOUNDS.register("parks_growl",
        () -> SoundEvent.createVariableRangeEvent(
            new ResourceLocation(NuclearParkMod.MOD_ID, "parks_growl")));

    public static final RegistryObject<SoundEvent> PARKS_DEATH = SOUNDS.register("parks_death",
        () -> SoundEvent.createVariableRangeEvent(
            new ResourceLocation(NuclearParkMod.MOD_ID, "parks_death")));
}
