package com.more_owleaf.entities.orb;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.more_owleaf.utils.OrbConfigManager;
import java.util.List;

public class OrbEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<String> CONFIG_DATA =
            SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.STRING);

    private OrbConfig config = new OrbConfig();
    private int spawnTimer = 0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public OrbEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CONFIG_DATA, "{}");
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            serverTick();
        }
    }

    private void serverTick() {
        switch (config.getMode()) {
            case BARRIER:
                handleBarrierMode();
                break;
            case SPAWNER:
                handleSpawnerMode();
                break;
            case IDLE:
                break;
        }
    }

    private void handleBarrierMode() {
        if (!config.isActive()) return;

        double size = config.getBarrierSize();
        double height = config.getBarrierHeight();

        AABB barrierArea = new AABB(
                position().add(-size, 0, -size),
                position().add(size, height, size)
        );

        List<Entity> entities = level().getEntitiesOfClass(Entity.class, barrierArea);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity &&
                    entity.getId() != this.getId() &&
                    !(entity instanceof OrbEntity)) {

                Vec3 entityPos = entity.position();
                Vec3 orbPos = this.position();

                double dx = entityPos.x - orbPos.x;
                double dz = entityPos.z - orbPos.z;
                double distance = Math.sqrt(dx * dx + dz * dz);

                if (distance < size && distance > 0) {
                    double pushX = (dx / distance) * 1.2;
                    double pushZ = (dz / distance) * 1.2;

                    entity.setDeltaMovement(pushX, entity.getDeltaMovement().y + 0.2, pushZ);
                    entity.hurtMarked = true;
                }
            }
        }
    }

    private void handleSpawnerMode() {
        if (!config.isActive()) return;

        spawnTimer++;
        if (spawnTimer >= config.getSpawnRate()) {
            if (trySpawnMob()) {
                spawnTimer = 0;
            } else {
                spawnTimer = Math.max(0, spawnTimer - 10);
            }
        }
    }

    private boolean trySpawnMob() {
        if (!(level() instanceof ServerLevel serverLevel)) return false;

        AABB spawnArea = new AABB(
                position().add(-8, -3, -8),
                position().add(8, 3, 8)
        );

        List<Entity> nearbyEntities = level().getEntitiesOfClass(Entity.class, spawnArea);
        long mobCount = nearbyEntities.stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof OrbEntity))
                .count();

        if (mobCount >= config.getMaxMobs()) {
            return false;
        }

        try {
            String mobName = config.getMobType();
            if (!mobName.contains(":")) {
                mobName = "minecraft:" + mobName;
            }

            ResourceLocation mobResource = new ResourceLocation(mobName);
            EntityType<?> mobType = BuiltInRegistries.ENTITY_TYPE.get(mobResource);

            if (mobType == null) {
                return false;
            }

            Entity mob = mobType.create(serverLevel);
            if (mob instanceof LivingEntity livingMob) {
                BlockPos spawnPos = findValidSpawnPosition();
                if (spawnPos == null) {
                    return false;
                }

                mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 0.1, spawnPos.getZ() + 0.5);

                if (livingMob instanceof Mob mobEntity) {
                    mobEntity.finalizeSpawn(serverLevel,
                            serverLevel.getCurrentDifficultyAt(spawnPos),
                            MobSpawnType.SPAWNER, null, null);
                }

                boolean spawned = serverLevel.addFreshEntity(mob);
                if (spawned) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            spawnPos.getX() + 0.5, spawnPos.getY() + 1, spawnPos.getZ() + 0.5,
                            20, 0.5, 0.5, 0.5, 0.1);

                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private BlockPos findValidSpawnPosition() {
        BlockPos basePos = this.blockPosition();

        for (int attempts = 0; attempts < 30; attempts++) {
            int x = basePos.getX() + (random.nextInt(9) - 4);
            int z = basePos.getZ() + (random.nextInt(9) - 4);

            for (int y = basePos.getY() + 3; y >= basePos.getY() - 3; y--) {
                BlockPos testPos = new BlockPos(x, y, z);
                BlockPos abovePos = testPos.above();
                BlockPos above2Pos = abovePos.above();

                boolean groundSolid = !level().getBlockState(testPos).isAir();
                boolean spaceEmpty = level().getBlockState(abovePos).isAir();
                boolean headEmpty = level().getBlockState(above2Pos).isAir();

                if (groundSolid && spaceEmpty && headEmpty) {
                    return abovePos;
                }
            }
        }

        return null;
    }

    public void setConfig(OrbConfig newConfig) {
        this.config = newConfig;
        this.spawnTimer = 0;
        syncConfigToClient();
    }

    public OrbConfig getConfig() {
        return config;
    }

    private void syncConfigToClient() {
        if (!level().isClientSide) {
            String configJson = OrbConfigManager.configToJson(config);
            this.entityData.set(CONFIG_DATA, configJson);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (CONFIG_DATA.equals(key) && level().isClientSide) {
            String configJson = this.entityData.get(CONFIG_DATA);
            this.config = OrbConfigManager.jsonToConfig(configJson);
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("OrbConfig")) {
            this.config = OrbConfigManager.jsonToConfig(tag.getString("OrbConfig"));
        }
        this.spawnTimer = tag.getInt("SpawnTimer");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("OrbConfig", OrbConfigManager.configToJson(config));
        tag.putInt("SpawnTimer", spawnTimer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<OrbEntity> state) {
        switch (config.getMode()) {
            case BARRIER:
                if (config.isActive()) {
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("barrier_open"));
                } else {
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                }
                break;
            case SPAWNER:
                if (config.isActive()) {
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("spawner_open"));
                } else {
                    state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                }
                break;
            case IDLE:
            default:
                state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                break;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
