package com.more_owleaf.client.models;

import com.more_owleaf.entities.orb.OrbEntity;
import com.more_owleaf.entities.orb.OrbConfig;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OrbModel extends GeoModel<OrbEntity> {
    private static final ResourceLocation MODEL_ORB = new ResourceLocation("more_owleaf", "geo/entity/orb.geo.json");
    private static final ResourceLocation MODEL_BARRIER = new ResourceLocation("more_owleaf", "geo/entity/barrier.geo.json");

    private static final ResourceLocation TEXTURE_ORB = new ResourceLocation("more_owleaf", "textures/entity/orb.png");
    private static final ResourceLocation TEXTURE_BARRIER = new ResourceLocation("more_owleaf", "textures/entity/barrier.png");

    private static final ResourceLocation ANIMATIONS = new ResourceLocation("more_owleaf", "animations/orb.animation.json");

    @Override
    public ResourceLocation getModelResource(OrbEntity entity) {
        OrbConfig config = entity.getConfig();

        if (config.getMode() == OrbConfig.OrbMode.BARRIER && config.isActive()) {
            return MODEL_BARRIER;
        }

        return MODEL_ORB;
    }

    @Override
    public ResourceLocation getTextureResource(OrbEntity entity) {
        OrbConfig config = entity.getConfig();

        if (config.getMode() == OrbConfig.OrbMode.BARRIER && config.isActive()) {
            return TEXTURE_BARRIER;
        }

        return TEXTURE_ORB;
    }

    @Override
    public ResourceLocation getAnimationResource(OrbEntity entity) {
        return ANIMATIONS;
    }
}
