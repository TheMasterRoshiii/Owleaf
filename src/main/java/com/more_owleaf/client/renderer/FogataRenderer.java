package com.more_owleaf.client.renderer;

import com.more_owleaf.client.models.FogataModel;
import com.more_owleaf.entities.FogataEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FogataRenderer extends GeoEntityRenderer<FogataEntity> {

public FogataRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FogataModel());
    }
}