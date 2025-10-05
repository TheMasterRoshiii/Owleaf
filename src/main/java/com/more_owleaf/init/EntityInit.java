package com.more_owleaf.init;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.entities.CasinoEntity;
import com.more_owleaf.entities.FogataEntity;
import com.more_owleaf.entities.RunaEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, More_Owleaf.MODID);

    public static final RegistryObject<EntityType<CasinoEntity>> CASINO = ENTITIES.register(
            "casino",
            () -> EntityType.Builder.of(CasinoEntity::new, MobCategory.MISC)
                    .sized(1.5f, 2.0f)
                    .clientTrackingRange(10)
                    .build("casino"));

    public static final RegistryObject<EntityType<FogataEntity>> FOGATA = ENTITIES.register(
            "fogata",
            () -> EntityType.Builder.of(FogataEntity::new, MobCategory.MISC)
                    .sized(3.0f, 3.0f)
                    .clientTrackingRange(10)
                    .build("fogata"));
    public static final RegistryObject<EntityType<RunaEntity>> RUNA_AMARILLA = ENTITIES.register(
            "runa_amarilla",
            () -> EntityType.Builder.of(RunaEntity::new, MobCategory.MISC)
                    .sized(1.0f, 2.0f)
                    .clientTrackingRange(10)
                    .build("runa_amarilla"));

    public static final RegistryObject<EntityType<RunaEntity>> RUNA_AZUL_CLARO = ENTITIES.register(
            "runa_azul_claro",
            () -> EntityType.Builder.of(RunaEntity::new, MobCategory.MISC)
                    .sized(1.0f, 2.0f)
                    .clientTrackingRange(10)
                    .build("runa_azul_claro"));

    public static final RegistryObject<EntityType<RunaEntity>> RUNA_MAGENTA = ENTITIES.register(
            "runa_magenta",
            () -> EntityType.Builder.of(RunaEntity::new, MobCategory.MISC)
                    .sized(1.0f, 2.0f)
                    .clientTrackingRange(10)
                    .build("runa_magenta"));

    public static final RegistryObject<EntityType<RunaEntity>> RUNA_MORADA = ENTITIES.register(
            "runa_morada",
            () -> EntityType.Builder.of(RunaEntity::new, MobCategory.MISC)
                    .sized(1.0f, 2.0f)
                    .clientTrackingRange(10)
                    .build("runa_morada"));

    public static final RegistryObject<EntityType<RunaEntity>> RUNA_ROJA = ENTITIES.register(
            "runa_roja",
            () -> EntityType.Builder.of(RunaEntity::new, MobCategory.MISC)
                    .sized(1.0f, 2.0f)
                    .clientTrackingRange(10)
                    .build("runa_roja"));

    public static final RegistryObject<EntityType<RunaEntity>> RUNA_VERDE = ENTITIES.register(
            "runa_verde",
            () -> EntityType.Builder.of(RunaEntity::new, MobCategory.MISC)
                    .sized(1.0f, 2.0f)
                    .clientTrackingRange(10)
                    .build("runa_verde"));
}