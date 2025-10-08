package com.more_owleaf.entities;

import com.more_owleaf.config.OrbConfig;
import com.more_owleaf.init.EntityInit;
import com.more_owleaf.network.NetworkHandler;
import com.more_owleaf.network.orb.OpenOrbScreenPacket;
import com.more_owleaf.utils.orb.OrbConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class OrbEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<String> CONFIG_DATA = SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_TRIGGER_SPAWN_ANIM =
            SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.BOOLEAN);

    private OrbConfig config = new OrbConfig();
    private boolean configInitialized = false;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int spawnTimer = 0;
    private int mobSpawnDelayTimer = -1;

    private int instantSpawnDelayCounter = -1;
    private int instantSpawnMobsRemaining = 0;
    private int instantSpawnBatchCooldown = 0;
    private int instantSpawnRadiusCache = 0;
    private String instantSpawnMobTypeCache = "";

    @Nullable
    private UUID barrierEntityUUID;

    public OrbEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    @Override public boolean canBeCollidedWith() { return true; }
    @Override public boolean isPickable() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CONFIG_DATA, "{}");
        this.entityData.define(DATA_TRIGGER_SPAWN_ANIM, false);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) { return InteractionResult.SUCCESS; }
        if (player.hasPermissions(2)) {
            if (player instanceof ServerPlayer serverPlayer) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OpenOrbScreenPacket(getId(), this.config));
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (this.entityData.get(DATA_TRIGGER_SPAWN_ANIM)) {
                this.entityData.set(DATA_TRIGGER_SPAWN_ANIM, false);
            }

            if (!configInitialized) {
                this.config = OrbConfigManager.loadOrbConfig(this.getUUID());
                OrbConfigManager.saveOrbConfig(this.getUUID(), this.config);
                this.configInitialized = true;
                syncConfigToClient();
            }

            if (config.getMode() == OrbConfig.OrbMode.SPAWNER && config.isActive()) {
                handleSpawnerMode();
            }

            handleInstantSpawnProcess();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide) {
            removeBarrier();
            OrbConfigManager.deleteOrbConfig(this.getUUID());
        }
        super.remove(reason);
    }

    public void setConfig(OrbConfig newConfig) {
        boolean wasBarrierActive = (this.config.getMode() == OrbConfig.OrbMode.BARRIER && this.config.isActive());
        this.config = newConfig;

        if (!level().isClientSide) {
            OrbConfigManager.saveOrbConfig(this.getUUID(), this.config);
            boolean isBarrierActiveNow = (this.config.getMode() == OrbConfig.OrbMode.BARRIER && this.config.isActive());

            if (wasBarrierActive && !isBarrierActiveNow) {
                removeBarrier();
            } else if (!wasBarrierActive && isBarrierActiveNow) {
                spawnBarrier();
            }
        }
        syncConfigToClient();
    }

    public void startInstantSpawn() {
        if (instantSpawnDelayCounter <= 0 && instantSpawnMobsRemaining <= 0) {
            this.instantSpawnDelayCounter = config.getInstantSpawnDelay();
            this.instantSpawnMobsRemaining = config.getInstantSpawnQuantity();
            this.instantSpawnRadiusCache = config.getInstantSpawnRadius();
            this.instantSpawnMobTypeCache = config.getMobType();
            this.instantSpawnBatchCooldown = 0;
        }
    }

    private void handleInstantSpawnProcess() {
        if (this.instantSpawnDelayCounter > 0) {
            this.instantSpawnDelayCounter--;
            return;
        }
        if (this.instantSpawnMobsRemaining > 0 && this.instantSpawnDelayCounter == 0) {
            this.instantSpawnBatchCooldown--;
            if (this.instantSpawnBatchCooldown <= 0) {
                spawnBatch();
                this.instantSpawnBatchCooldown = 3;
            }
        }
    }

    private void spawnBatch() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        EntityType<?> entityTypeToSpawn = EntityType.byString(this.instantSpawnMobTypeCache).orElse(null);
        if (entityTypeToSpawn == null) {
            this.instantSpawnMobsRemaining = 0;
            return;
        }
        int mobsToSpawnInThisBatch = Math.min(10, this.instantSpawnMobsRemaining);
        for (int i = 0; i < mobsToSpawnInThisBatch; i++) {
            Entity entity = entityTypeToSpawn.create(serverLevel);
            if (entity instanceof Mob mob) {
                if (config.getChargeCreepers() && mob instanceof Creeper creeper) {
                    creeper.thunderHit((ServerLevel) mob.level(), null);
                }
                BlockPos spawnPos = findValidSpawnPosition(serverLevel, this.instantSpawnRadiusCache);
                if (spawnPos != null) {
                    mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWNER, null, null);
                    serverLevel.addFreshEntity(mob);
                }
            }
        }
        this.instantSpawnMobsRemaining -= mobsToSpawnInThisBatch;
        if (this.instantSpawnMobsRemaining <= 0) {
            this.instantSpawnDelayCounter = -1;
        }
    }

    private void spawnBarrier() {
        if (level() instanceof ServerLevel serverLevel && barrierEntityUUID == null) {
            final double verticalOffset = 17.0;
            boolean orientation = this.config.isBarrierZAligned();
            spawnWallAtHeight(serverLevel, 0, orientation);
            spawnWallAtHeight(serverLevel, verticalOffset, orientation);
            this.barrierEntityUUID = this.getUUID();
        }
    }

    private void spawnWallAtHeight(ServerLevel level, double yOffset, boolean isZAligned) {
        float segmentLength = EntityInit.BARRIER_ENTITY.get().getDimensions().width;
        int totalSegments = 2;
        final float spacing = -7.0f;
        float step = segmentLength + spacing;
        float totalLength = (step * (totalSegments - 1)) + segmentLength;
        double startOffset = -(totalLength / 2) + (segmentLength / 2);
        for (int i = 0; i < totalSegments; i++) {
            double currentOffset = startOffset + (i * step);
            double spawnX = this.getX();
            double spawnY = (this.getY() - 15) + yOffset;
            double spawnZ = this.getZ();
            if (isZAligned) spawnZ += currentOffset; else spawnX += currentOffset;
            spawnWallSegment(level, spawnX, spawnY, spawnZ, isZAligned);
        }
    }

    private void spawnWallSegment(ServerLevel level, double x, double y, double z, boolean isZAligned) {
        BarrierEntity wall = new BarrierEntity(EntityInit.BARRIER_ENTITY.get(), level);
        wall.setPos(x, y, z);
        wall.setOwner(this.getUUID());
        wall.setOrientation(isZAligned);
        wall.recalculateBoundingBox();
        level.addFreshEntity(wall);
    }

    private void removeBarrier() {
        if (level() instanceof ServerLevel serverLevel && barrierEntityUUID != null) {
            serverLevel.getEntitiesOfClass(BarrierEntity.class, this.getBoundingBox().inflate(64))
                    .stream()
                    .filter(barrier -> this.getUUID().equals(barrier.getOwnerUUID()))
                    .forEach(Entity::discard);
            this.barrierEntityUUID = null;
        }
    }

    private void handleSpawnerMode() {
        if (this.mobSpawnDelayTimer > 0) {
            this.mobSpawnDelayTimer--;
            if (this.mobSpawnDelayTimer == 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    trySpawnMobs(serverLevel);
                }
            }
            return;
        }
        if (this.spawnTimer > 0) {
            this.spawnTimer--;
            return;
        }
        this.spawnTimer = config.getSpawnRate();
        this.entityData.set(DATA_TRIGGER_SPAWN_ANIM, true);
        this.mobSpawnDelayTimer = 20;
    }

    private boolean trySpawnMobs(ServerLevel serverLevel) {
        AABB checkArea = new AABB(this.blockPosition()).inflate(config.getSpawnRadius());
        EntityType<?> entityTypeToSpawn = EntityType.byString(config.getMobType()).orElse(null);
        if (entityTypeToSpawn == null) return false;
        long existingMobs = serverLevel.getEntitiesOfClass(LivingEntity.class, checkArea, (e) -> e.getType() == entityTypeToSpawn).size();
        if (existingMobs >= config.getMaxMobs()) return false;
        boolean spawnedAtLeastOne = false;
        for (int i = 0; i < config.getSpawnCount(); i++) {
            if (existingMobs + i >= config.getMaxMobs()) break;
            Entity entity = entityTypeToSpawn.create(serverLevel);
            if (entity instanceof Mob mob) {
                if (config.getChargeCreepers() && mob instanceof Creeper creeper) {
                    creeper.thunderHit((ServerLevel) mob.level(), null);
                }
                BlockPos spawnPos = findValidSpawnPosition(serverLevel, config.getSpawnRadius());
                if (spawnPos != null) {
                    mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWNER, null, null);
                    if (serverLevel.addFreshEntity(mob)) {
                        spawnedAtLeastOne = true;
                    }
                }
            }
        }
        return spawnedAtLeastOne;
    }

    private BlockPos findValidSpawnPosition(ServerLevel level, int radius) {
        for (int i = 0; i < 20; i++) {
            int x = this.getBlockX() + this.random.nextInt(radius * 2 + 1) - radius;
            int y = this.getBlockY() + this.random.nextInt(5) - 2;
            int z = this.getBlockZ() + this.random.nextInt(radius * 2 + 1) - radius;
            BlockPos pos = new BlockPos(x, y, z);
            if (level.noCollision(new AABB(pos)) && !level.getBlockState(pos.below()).isAir()) {
                return pos;
            }
        }
        return null;
    }

    public OrbConfig getConfig() { return config; }
    private void syncConfigToClient() { if (!level().isClientSide) { this.entityData.set(CONFIG_DATA, OrbConfigManager.configToJson(config)); } }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CONFIG_DATA.equals(key) && level().isClientSide) {
            this.config = OrbConfigManager.jsonToConfig(this.entityData.get(CONFIG_DATA));
        }
        if (DATA_TRIGGER_SPAWN_ANIM.equals(key) && level().isClientSide) {
            if (this.entityData.get(DATA_TRIGGER_SPAWN_ANIM)) {
                this.triggerAnim("main", "spawner_open");
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.configInitialized = tag.getBoolean("ConfigInitialized");
        if (tag.contains("OrbConfig")) { this.config = OrbConfigManager.jsonToConfig(tag.getString("OrbConfig")); }
        if (tag.hasUUID("BarrierUUID")) { this.barrierEntityUUID = tag.getUUID("BarrierUUID"); }
        this.spawnTimer = tag.getInt("SpawnTimer");
        this.mobSpawnDelayTimer = tag.getInt("MobSpawnDelayTimer");
        this.instantSpawnDelayCounter = tag.getInt("InstantSpawnDelayCounter");
        this.instantSpawnMobsRemaining = tag.getInt("InstantSpawnMobsRemaining");
        this.instantSpawnBatchCooldown = tag.getInt("InstantSpawnBatchCooldown");
        this.instantSpawnRadiusCache = tag.getInt("InstantSpawnRadiusCache");
        this.instantSpawnMobTypeCache = tag.getString("InstantSpawnMobTypeCache");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("ConfigInitialized", this.configInitialized);
        tag.putString("OrbConfig", OrbConfigManager.configToJson(config));
        if (barrierEntityUUID != null) { tag.putUUID("BarrierUUID", barrierEntityUUID); }
        tag.putInt("SpawnTimer", spawnTimer);
        tag.putInt("MobSpawnDelayTimer", mobSpawnDelayTimer);
        tag.putInt("InstantSpawnDelayCounter", this.instantSpawnDelayCounter);
        tag.putInt("InstantSpawnMobsRemaining", this.instantSpawnMobsRemaining);
        tag.putInt("InstantSpawnBatchCooldown", this.instantSpawnBatchCooldown);
        tag.putInt("InstantSpawnRadiusCache", this.instantSpawnRadiusCache);
        tag.putString("InstantSpawnMobTypeCache", this.instantSpawnMobTypeCache);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<OrbEntity> controller = new AnimationController<>(this, "main", 0, this::animationPredicate);
        controller.setTransitionLength(10);
        controller.triggerableAnim("spawner_open", RawAnimation.begin().thenPlay("spawner_open"));
        controllers.add(controller);
    }

    private PlayState animationPredicate(AnimationState<OrbEntity> state) {
        state.getController().setAnimation(RawAnimation.begin().thenLoop(getAnimationForCurrentState()));
        return PlayState.CONTINUE;
    }

    private String getAnimationForCurrentState() {
        switch (config.getMode()) {
            case IDLE: return config.isActive() ? "idle" : "idle_close";
            case BARRIER: return config.isActive() ? "barrier_open" : "barrier_close";
            case SPAWNER: return "spawner_close";
            default: return "idle";
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}