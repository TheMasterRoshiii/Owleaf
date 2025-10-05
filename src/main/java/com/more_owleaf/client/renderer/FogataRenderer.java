package com.more_owleaf.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.more_owleaf.client.models.FogataModel;
import com.more_owleaf.entities.FogataEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FogataRenderer extends GeoEntityRenderer<FogataEntity> {

    public FogataRenderer(EntityRendererProvider.Context context) {
        super(context, new FogataModel());
    }
    public void render(FogataEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float actualYaw = entity.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - actualYaw));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}