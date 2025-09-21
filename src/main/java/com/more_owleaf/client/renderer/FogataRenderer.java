package com.more_owleaf.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.more_owleaf.More_Owleaf;
import com.more_owleaf.client.SkinHelper;
import com.more_owleaf.client.models.FogataModel;
import com.more_owleaf.config.DeathRegistry;
import com.more_owleaf.entities.FogataEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.List;

public class FogataRenderer extends GeoEntityRenderer<FogataEntity> {
    private final PlayerModel<?> playerModel;

    public FogataRenderer(EntityRendererProvider.Context context) {
        super(context, new FogataModel());
        this.playerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
    }

    @Override
    public void render(FogataEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        renderDeadPlayerHeads(entity, poseStack, buffer, packedLight, partialTicks);
    }

    private void renderDeadPlayerHeads(FogataEntity entity, PoseStack poseStack, MultiBufferSource buffer,
                                       int packedLight, float partialTicks) {
        if (More_Owleaf.DEATH_REGISTRY == null) return;

        List<DeathRegistry.DeathRecord> deaths = More_Owleaf.DEATH_REGISTRY.getDeathRecords();
        int maxHeads = Math.min(deaths.size(), 12);

        if (maxHeads == 0) return;

        for (int i = 0; i < maxHeads; i++) {
            DeathRegistry.DeathRecord death = deaths.get(i);

            poseStack.pushPose();

            float angle = (float) (i * 2 * Math.PI / maxHeads);
            float baseRadius = 1.4f;
            float radiusVariation = (float) Math.sin((entity.tickCount + partialTicks) * 0.02f + i * 0.5f) * 0.15f;
            float radius = baseRadius + radiusVariation;

            float baseHeight = 1.6f;
            float heightVariation = (float) Math.sin((entity.tickCount + partialTicks + i * 30) * 0.025f) * 0.2f;
            float height = baseHeight + heightVariation;

            float rotationSpeed = 0.008f;
            float x = (float) (Math.cos(angle + (entity.tickCount + partialTicks) * rotationSpeed) * radius);
            float z = (float) (Math.sin(angle + (entity.tickCount + partialTicks) * rotationSpeed) * radius);

            poseStack.translate(x, height, z);

            float scale = 0.4f + (float) Math.sin((entity.tickCount + partialTicks) * 0.03f + i) * 0.05f;
            poseStack.scale(scale, scale, scale);

            float yRotation = (entity.tickCount + partialTicks) * 1.2f + i * 30;
            float xTilt = (float) Math.sin((entity.tickCount + partialTicks) * 0.015f + i) * 8;

            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRotation));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xTilt));

            renderPlayerHead(poseStack, buffer, death, packedLight);

            poseStack.popPose();
        }
    }

    private void renderPlayerHead(PoseStack poseStack, MultiBufferSource buffer, DeathRegistry.DeathRecord death, int packedLight) {
        try {
            RenderSystem.enableBlend();

            ResourceLocation skinTexture = SkinHelper.getPlayerSkinFromData(
                    death.playerName,
                    death.playerUUID,
                    death.skinTextureData,
                    death.skinSignature,
                    death.hasSkinData
            );

            if (skinTexture == null) {
                return;
            }

            RenderType renderType = RenderType.entityTranslucent(skinTexture);
            VertexConsumer vertexConsumer = buffer.getBuffer(renderType);

            int enhancedLight = Math.min(packedLight + 60, 255);
            int overlay = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;

            playerModel.head.render(poseStack, vertexConsumer, enhancedLight, overlay);
            playerModel.hat.render(poseStack, vertexConsumer, enhancedLight, overlay);

        } catch (Exception e) {
        }
    }
}
