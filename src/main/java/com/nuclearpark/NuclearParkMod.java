package com.nuclearpark;

import com.nuclearpark.item.NuclearBombItem;
import com.nuclearpark.entity.ParksEntity;
import com.nuclearpark.entity.ModEntities;
import com.nuclearpark.client.renderer.ParksRenderer;
import com.nuclearpark.sound.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("nuclearpark")
public class NuclearParkMod {
    public static final String MOD_ID = "nuclearpark";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> NUCLEAR_BOMB = ITEMS.register("nuclear_bomb", 
        () -> new NuclearBombItem(new Item.Properties().stacksTo(1)));

    public NuclearParkMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerEntityAttributes);

        ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::registerEntityRenderers);
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("NuclearPark mod loaded! Watch out for Parks!");
    }

    private void addCreative(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.COMBAT) {
            event.accept(NUCLEAR_BOMB);
        }
    }

    public void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.PARKS.get(), ParksEntity.createAttributes().build());
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PARKS.get(), ParksRenderer::new);
    }
}
