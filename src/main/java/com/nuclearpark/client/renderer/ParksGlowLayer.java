package com.nuclearpark.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nuclearpark.NuclearParkMod;
import com.nuclearpark.entity.ParksEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ParksGlowLayer extends EyesLayer<ParksEntity, ParksModel<ParksEntity>> {
    private static final RenderType GLOW_RENDER_TYPE = RenderType.eyes(
        new ResourceLocation(NuclearParkMod.MOD_ID, "textures/entity/parks_glow.png"));

    public ParksGlowLayer(RenderLayerParent<ParksEntity, ParksModel<ParksEntity>> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW_RENDER_TYPE;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, 
                       int packedLight, ParksEntity entity, 
                       float limbSwing, float limbSwingAmount, 
                       float partialTick, float ageInTicks, 
                       float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.renderType());
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 
            15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
