package com.more_owleaf.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.more_owleaf.client.models.RunaModel;
import com.more_owleaf.entities.RunaEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RunaRenderer extends GeoEntityRenderer<RunaEntity> {

    public RunaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RunaModel());
    }

    public void render(RunaEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float actualYaw = entity.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - actualYaw));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}