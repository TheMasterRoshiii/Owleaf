package com.more_owleaf.client.models;

import com.more_owleaf.entities.OrbEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OrbModel extends GeoModel<OrbEntity> {
    private static final ResourceLocation MODEL_ORB = new ResourceLocation("more_owleaf", "geo/entity/orb.geo.json");
    private static final ResourceLocation TEXTURE_ORB = new ResourceLocation("more_owleaf", "textures/entity/orb.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation("more_owleaf", "animations/orb.animation.json");

    @Override
    public ResourceLocation getModelResource(OrbEntity entity) {
        return MODEL_ORB;
    }

    @Override
    public ResourceLocation getTextureResource(OrbEntity entity) {
        return TEXTURE_ORB;
    }

    @Override
    public ResourceLocation getAnimationResource(OrbEntity entity) {
        return ANIMATIONS;
    }
}