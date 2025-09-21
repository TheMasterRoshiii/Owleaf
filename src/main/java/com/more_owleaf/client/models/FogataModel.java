package com.more_owleaf.client.models;

import com.more_owleaf.entities.FogataEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FogataModel extends GeoModel<FogataEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.parse("more_owleaf:geo/entity/fogata.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("more_owleaf:textures/entity/fogata.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.parse("more_owleaf:animations/fogata.animation.json");

    @Override
    public ResourceLocation getModelResource(FogataEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FogataEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FogataEntity animatable) {
        return ANIMATIONS;
    }
}