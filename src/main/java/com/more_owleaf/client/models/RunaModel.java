package com.more_owleaf.client.models;

import com.more_owleaf.entities.RunaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RunaModel extends GeoModel<RunaEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.parse("more_owleaf:geo/entity/runa.geo.json");

    private static final ResourceLocation TEXTURE_YELLOW = ResourceLocation.parse("more_owleaf:textures/entity/runa_amarilla.png");
    private static final ResourceLocation TEXTURE_LIGHT_BLUE = ResourceLocation.parse("more_owleaf:textures/entity/runa_azul_claro.png");
    private static final ResourceLocation TEXTURE_MAGENTA = ResourceLocation.parse("more_owleaf:textures/entity/runa_magenta.png");
    private static final ResourceLocation TEXTURE_PURPLE = ResourceLocation.parse("more_owleaf:textures/entity/runa_morada.png");
    private static final ResourceLocation TEXTURE_RED = ResourceLocation.parse("more_owleaf:textures/entity/runa_roja.png");
    private static final ResourceLocation TEXTURE_GREEN = ResourceLocation.parse("more_owleaf:textures/entity/runa_verde.png");

    @Override
    public ResourceLocation getModelResource(RunaEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RunaEntity animatable) {
        switch (animatable.getColorVariant()) {
            case RunaEntity.VARIANT_YELLOW: return TEXTURE_YELLOW;
            case RunaEntity.VARIANT_LIGHT_BLUE: return TEXTURE_LIGHT_BLUE;
            case RunaEntity.VARIANT_MAGENTA: return TEXTURE_MAGENTA;
            case RunaEntity.VARIANT_PURPLE: return TEXTURE_PURPLE;
            case RunaEntity.VARIANT_RED: return TEXTURE_RED;
            case RunaEntity.VARIANT_GREEN: return TEXTURE_GREEN;
            default: return TEXTURE_YELLOW;
        }
    }

    @Override
    public ResourceLocation getAnimationResource(RunaEntity animatable) {
        return null;
    }
}