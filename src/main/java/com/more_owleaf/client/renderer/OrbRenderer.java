package com.more_owleaf.client.renderer;

import com.more_owleaf.client.models.OrbModel;
import com.more_owleaf.entities.orb.OrbEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OrbRenderer extends GeoEntityRenderer<OrbEntity> {
    public OrbRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new OrbModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public float getMotionAnimThreshold(OrbEntity animatable) {
        return 0.005f;
    }
}
