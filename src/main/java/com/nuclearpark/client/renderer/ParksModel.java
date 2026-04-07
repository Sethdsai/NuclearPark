package com.nuclearpark.client.renderer;

import com.nuclearpark.NuclearParkMod;
import com.nuclearpark.entity.ParksEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ParksModel<T extends ParksEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = 
        new ModelLayerLocation(new ResourceLocation(NuclearParkMod.MOD_ID, "parks"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart legs;

    public ParksModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.legs = root.getChild("legs");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", 
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6.0F, -8.0F, -4.0F, 12.0F, 10.0F, 8.0F, 
                    new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 14.0F, 2.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(0, 18)
                .addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F, 
                    new CubeDeformation(0.0F))
                .texOffs(28, 0)
                .addBox(-2.0F, -1.0F, -8.0F, 4.0F, 4.0F, 2.0F, // Jaws
                    new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 6.0F, -8.0F));

        PartDefinition legs = partdefinition.addOrReplaceChild("legs",
            CubeListBuilder.create()
                .texOffs(44, 0)
                .addBox(-5.0F, 0.0F, -2.0F, 3.0F, 8.0F, 3.0F) // Front left
                .addBox(2.0F, 0.0F, -2.0F, 3.0F, 8.0F, 3.0F)  // Front right
                .addBox(-5.0F, 0.0F, 4.0F, 3.0F, 8.0F, 3.0F)  // Back left
                .addBox(2.0F, 0.0F, 4.0F, 3.0F, 8.0F, 3.0F),  // Back right
            PartPose.offset(0.0F, 16.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, 
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        
        // Walking animation
        this.body.xRot = (float)(Math.sin(limbSwing * 0.5F) * limbSwingAmount * 0.2F);
        this.legs.xRot = (float)(Math.sin(limbSwing * 1.5F) * limbSwingAmount * 0.8F);
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, 
                               net.minecraft.client.renderer.MultiBufferSource buffer, 
                               int packedLight, int packedOverlay, 
                               float red, float green, float blue, float alpha) {
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        legs.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
