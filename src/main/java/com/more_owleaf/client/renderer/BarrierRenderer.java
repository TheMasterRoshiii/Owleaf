package com.more_owleaf.client.renderer;

import com.more_owleaf.client.models.BarrierModel;
import com.more_owleaf.entities.BarrierEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BarrierRenderer extends GeoEntityRenderer<BarrierEntity> {
    public BarrierRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BarrierModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(BarrierEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
    }
}