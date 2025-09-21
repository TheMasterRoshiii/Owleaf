package com.more_owleaf.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.more_owleaf.client.models.CasinoModel;
import com.more_owleaf.entities.CasinoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CasinoRenderer extends GeoEntityRenderer<CasinoEntity> {

    public CasinoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CasinoModel());
    }

    public void render(CasinoEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float actualYaw = entity.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - actualYaw));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}