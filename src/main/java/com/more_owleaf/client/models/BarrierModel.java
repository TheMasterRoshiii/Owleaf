package com.more_owleaf.client.models;

import com.more_owleaf.entities.BarrierEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BarrierModel extends GeoModel<BarrierEntity> {
    private static final ResourceLocation MODEL_BARRIER = new ResourceLocation("more_owleaf", "geo/entity/barrier.geo.json");
    private static final ResourceLocation TEXTURE_BARRIER = new ResourceLocation("more_owleaf", "textures/entity/orb.png");

    @Override
    public ResourceLocation getModelResource(BarrierEntity object) {
        return MODEL_BARRIER;
    }

    @Override
    public ResourceLocation getTextureResource(BarrierEntity object) {
        return TEXTURE_BARRIER;
    }

    @Override
    public ResourceLocation getAnimationResource(BarrierEntity animatable) {
        return null;
    }
}
