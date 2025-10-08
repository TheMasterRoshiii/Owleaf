package com.more_owleaf.client.renderer;

import com.mojang.math.Axis;
import com.more_owleaf.client.models.OrbModel;
import com.more_owleaf.entities.OrbEntity;
import com.more_owleaf.config.OrbConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OrbRenderer extends GeoEntityRenderer<OrbEntity> {
    public OrbRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new OrbModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public void preRender(PoseStack poseStack, OrbEntity entity, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, float red,
                          float green, float blue, float alpha) {

        OrbConfig config = entity.getConfig();

        if (config.getMode() == OrbConfig.OrbMode.BARRIER) {
            if (config.isBarrierZAligned()) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }
        if (config.getMode() == OrbConfig.OrbMode.BARRIER && config.isActive()) {
            alpha = 0.7F;
        }

        float scale = 1.0F;
        this.scaleHeight = scale;
        this.scaleWidth = scale;

        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void postRender(PoseStack poseStack, OrbEntity entity, BakedGeoModel model,
                           MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                           float partialTick, int packedLight, int packedOverlay, float red,
                           float green, float blue, float alpha) {

        super.postRender(poseStack, entity, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        OrbConfig config = entity.getConfig();

        if (config.getMode() == OrbConfig.OrbMode.BARRIER && config.isActive()) {
            RenderSystem.enableCull();
            GL11.glEnable(GL11.GL_CULL_FACE);
        }
    }

    @Override
    public RenderType getRenderType(OrbEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        OrbConfig config = animatable.getConfig();

        if (config.getMode() == OrbConfig.OrbMode.BARRIER && config.isActive()) {
            return RenderType.entityTranslucent(getTextureLocation(animatable));
        } else {
            return RenderType.entityCutout(getTextureLocation(animatable));
        }
    }

    @Override
    protected float getDeathMaxRotation(OrbEntity animatable) {
        return 0.0f;
    }

    @Override
    public float getMotionAnimThreshold(OrbEntity animatable) {
        return 0.005f;
    }
}