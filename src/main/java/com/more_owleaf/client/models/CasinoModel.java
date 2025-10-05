package com.more_owleaf.client.models;

import com.more_owleaf.entities.CasinoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CasinoModel extends GeoModel<CasinoEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.parse("more_owleaf:geo/entity/casino.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("more_owleaf:textures/entity/casino.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.parse("more_owleaf:animations/casino.animation.json");

    @Override
    public ResourceLocation getModelResource(CasinoEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CasinoEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CasinoEntity animatable) {
        return ANIMATIONS;
    }
}