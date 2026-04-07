package com.nuclearpark.client.renderer;

import com.nuclearpark.NuclearParkMod;
import com.nuclearpark.entity.ParksEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ParksRenderer extends MobRenderer<ParksEntity, ParksModel<ParksEntity>> {
    private static final ResourceLocation PARKS_TEXTURE = 
        new ResourceLocation(NuclearParkMod.MOD_ID, "textures/entity/parks.png");

    public ParksRenderer(EntityRendererProvider.Context context) {
        super(context, new ParksModel<>(context.bakeLayer(ParksModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new ParksGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(ParksEntity entity) {
        return PARKS_TEXTURE;
    }
}
