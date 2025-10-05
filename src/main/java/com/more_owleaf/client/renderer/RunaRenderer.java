package com.more_owleaf.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.more_owleaf.client.models.RunaModel;
import com.more_owleaf.entities.RunaEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

public class RunaRenderer extends GeoEntityRenderer<RunaEntity> {

    public RunaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RunaModel());
    }

    @Override
    public void render(RunaEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float actualYaw = entity.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - actualYaw));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        renderFloatingItem(entity, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderFloatingItem(RunaEntity entity, float partialTick, PoseStack poseStack,
                                    MultiBufferSource bufferSource, int packedLight) {
        ItemStack itemRenderer = entity.getItemRenderer();
        if (itemRenderer.isEmpty()) return;

        poseStack.pushPose();

        poseStack.translate(0.0D, 2.0D, 0.0D);

        float floatingOffset = Mth.sin((entity.tickCount + partialTick) * 0.1F) * 0.1F + 0.1F;
        poseStack.translate(0.0D, floatingOffset, 0.0D);

        float rotation = (entity.tickCount + partialTick) * 2.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        float scale = 1.5F;
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                itemRenderer,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
    }
}