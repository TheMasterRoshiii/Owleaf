package com.more_owleaf.entities;

import com.more_owleaf.config.OrbConfig;
import com.more_owleaf.entities.BarrierEntity;
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
import net.minecraft.world.damagesource.DamageSource;
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
    private static final EntityDataAccessor<Integer> FORCE_SYNC = SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_HEALTH = SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_SPAWNING_ANIMATION = SynchedEntityData.defineId(OrbEntity.class, EntityDataSerializers.BOOLEAN);

    private OrbConfig config = new OrbConfig();
    private boolean configInitialized = false;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int syncCounter = 0;

    private int spawnTimer = 0;
    private int mobSpawnDelayTimer = -1;
    private boolean isCurrentlySpawning = false;
    private int spawnAnimationTimer = 0;

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
        this.setNoGravity(true);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CONFIG_DATA, "{}");
        this.entityData.define(FORCE_SYNC, 0);
        this.entityData.define(HEALTH, 100.0F);
        this.entityData.define(MAX_HEALTH, 100.0F);
        this.entityData.define(IS_SPAWNING_ANIMATION, false);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }


    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean canTakeDamage = (config.getMode() == OrbConfig.OrbMode.SPAWNER || config.getMode() == OrbConfig.OrbMode.BARRIER)
                && config.isDamageable();

        if (!canTakeDamage) {
            return false;
        }

        float currentHealth = this.entityData.get(HEALTH);
        float finalDamage = amount * (1.0f - config.getResistance());
        float newHealth = Math.max(0, currentHealth - finalDamage);

        this.entityData.set(HEALTH, newHealth);

        if (newHealth <= 0) {
            this.remove(RemovalReason.KILLED);
        }

        return true;
    }

    public float getHealth() {
        return this.entityData.get(HEALTH);
    }

    public void setHealth(float health) {
        this.entityData.set(HEALTH, Math.max(0, Math.min(health, this.entityData.get(MAX_HEALTH))));
    }

    public float getMaxHealth() {
        return this.entityData.get(MAX_HEALTH);
    }

    public void setMaxHealth(float maxHealth) {
        this.entityData.set(MAX_HEALTH, maxHealth);
        if (getHealth() > maxHealth) {
            setHealth(maxHealth);
        }
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

        this.setDeltaMovement(0, 0, 0);
        this.fallDistance = 0.0F;

        if (!level().isClientSide) {
            if (!configInitialized) {
                this.config = OrbConfigManager.loadOrbConfig(this.getUUID());
                OrbConfigManager.saveOrbConfig(this.getUUID(), this.config);
                this.configInitialized = true;
                updateHealthAttributes();
                forceSync();
            }

            handleSpawnerLogic();
            handleSpawnAnimation();
            handleInstantSpawnProcess();
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        this.setDeltaMovement(0, 0, 0);
    }

    private void updateHealthAttributes() {
        if (!level().isClientSide) {
            setMaxHealth(config.getMaxHealth());
            setHealth(config.getMaxHealth());
        }
    }

    private void handleSpawnerLogic() {
        boolean shouldBeSpawning = (config.getMode() == OrbConfig.OrbMode.SPAWNER && config.isActive());

        if (!shouldBeSpawning) {
            if (isCurrentlySpawning) {
                stopSpawning();
            }
            return;
        }

        if (this.mobSpawnDelayTimer > 0) {
            this.mobSpawnDelayTimer--;
            if (this.mobSpawnDelayTimer == 0) {
                startSpawnAnimation();
                trySpawnMobs();
            }
            return;
        }

        if (this.spawnTimer > 0) {
            this.spawnTimer--;
            return;
        }

        startSpawnCycle();
    }

    private void handleSpawnAnimation() {
        if (this.spawnAnimationTimer > 0) {
            this.spawnAnimationTimer--;
            if (this.spawnAnimationTimer == 0) {
                this.entityData.set(IS_SPAWNING_ANIMATION, false);
            }
        }
    }

    private void startSpawnAnimation() {
        this.entityData.set(IS_SPAWNING_ANIMATION, true);
        this.spawnAnimationTimer = 40;
    }

    private void startSpawnCycle() {
        this.spawnTimer = config.getSpawnRate();
        this.mobSpawnDelayTimer = 20;
        this.isCurrentlySpawning = true;
    }

    private void stopSpawning() {
        this.isCurrentlySpawning = false;
        this.spawnTimer = 0;
        this.mobSpawnDelayTimer = -1;
        this.spawnAnimationTimer = 0;
        this.entityData.set(IS_SPAWNING_ANIMATION, false);
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

        boolean spawnerConfigChanged = false;
        if (this.config.getMode() == OrbConfig.OrbMode.SPAWNER && newConfig.getMode() == OrbConfig.OrbMode.SPAWNER) {
            spawnerConfigChanged =
                    !this.config.getMobType().equals(newConfig.getMobType()) ||
                            this.config.getSpawnRate() != newConfig.getSpawnRate() ||
                            this.config.getSpawnCount() != newConfig.getSpawnCount() ||
                            this.config.getMaxMobs() != newConfig.getMaxMobs() ||
                            this.config.getSpawnRadius() != newConfig.getSpawnRadius() ||
                            this.config.getChargeCreepers() != newConfig.getChargeCreepers() ||
                            this.config.getMaxHealth() != newConfig.getMaxHealth() ||
                            this.config.getResistance() != newConfig.getResistance() ||
                            this.config.isDamageable() != newConfig.isDamageable();
        }

        this.config = newConfig;

        if (!level().isClientSide) {
            updateHealthAttributes();
            OrbConfigManager.saveOrbConfig(this.getUUID(), this.config);
            boolean isBarrierActiveNow = (this.config.getMode() == OrbConfig.OrbMode.BARRIER && this.config.isActive());

            if (wasBarrierActive && !isBarrierActiveNow) {
                removeBarrier();
            } else if (!wasBarrierActive && isBarrierActiveNow) {
                spawnBarrier();
            }

            if (this.config.getMode() != OrbConfig.OrbMode.SPAWNER || !this.config.isActive()) {
                stopSpawning();
            }

            if (spawnerConfigChanged && this.config.getMode() == OrbConfig.OrbMode.SPAWNER && this.config.isActive()) {
                stopSpawning();
                startSpawnCycle();
            }
        }

        forceSync();
    }

    private void forceSync() {
        if (!level().isClientSide) {
            this.syncCounter++;
            this.entityData.set(CONFIG_DATA, OrbConfigManager.configToJson(config));
            this.entityData.set(FORCE_SYNC, this.syncCounter);
        }
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
                startSpawnAnimation();
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
                    CompoundTag nbt = new CompoundTag();
                    creeper.addAdditionalSaveData(nbt);
                    nbt.putBoolean("powered", true);
                    creeper.readAdditionalSaveData(nbt);
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

    private void trySpawnMobs() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        AABB checkArea = new AABB(this.blockPosition()).inflate(config.getSpawnRadius());
        EntityType<?> entityTypeToSpawn = EntityType.byString(config.getMobType()).orElse(null);
        if (entityTypeToSpawn == null) return;

        long existingMobs = serverLevel.getEntitiesOfClass(LivingEntity.class, checkArea, (e) -> e.getType() == entityTypeToSpawn).size();
        if (existingMobs >= config.getMaxMobs()) return;

        for (int i = 0; i < config.getSpawnCount(); i++) {
            if (existingMobs + i >= config.getMaxMobs()) break;

            Entity entity = entityTypeToSpawn.create(serverLevel);
            if (entity instanceof Mob mob) {
                if (config.getChargeCreepers() && mob instanceof Creeper creeper) {
                    CompoundTag nbt = new CompoundTag();
                    creeper.addAdditionalSaveData(nbt);
                    nbt.putBoolean("powered", true);
                    creeper.readAdditionalSaveData(nbt);
                }

                BlockPos spawnPos = findValidSpawnPosition(serverLevel, config.getSpawnRadius());
                if (spawnPos != null) {
                    mob.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWNER, null, null);
                    serverLevel.addFreshEntity(mob);
                }
            }
        }
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

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CONFIG_DATA.equals(key) && level().isClientSide) {
            this.config = OrbConfigManager.jsonToConfig(this.entityData.get(CONFIG_DATA));
        }
        if (FORCE_SYNC.equals(key) && level().isClientSide) {
            this.config = OrbConfigManager.jsonToConfig(this.entityData.get(CONFIG_DATA));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.configInitialized = tag.getBoolean("ConfigInitialized");
        if (tag.contains("OrbConfig")) { this.config = OrbConfigManager.jsonToConfig(tag.getString("OrbConfig")); }
        if (tag.hasUUID("BarrierUUID")) { this.barrierEntityUUID = tag.getUUID("BarrierUUID"); }
        this.spawnTimer = tag.getInt("SpawnTimer");
        this.mobSpawnDelayTimer = tag.getInt("MobSpawnDelayTimer");
        this.isCurrentlySpawning = tag.getBoolean("IsCurrentlySpawning");
        this.spawnAnimationTimer = tag.getInt("SpawnAnimationTimer");
        this.instantSpawnDelayCounter = tag.getInt("InstantSpawnDelayCounter");
        this.instantSpawnMobsRemaining = tag.getInt("InstantSpawnMobsRemaining");
        this.instantSpawnBatchCooldown = tag.getInt("InstantSpawnBatchCooldown");
        this.instantSpawnRadiusCache = tag.getInt("InstantSpawnRadiusCache");
        this.instantSpawnMobTypeCache = tag.getString("InstantSpawnMobTypeCache");
        this.syncCounter = tag.getInt("SyncCounter");
        this.entityData.set(HEALTH, tag.getFloat("Health"));
        this.entityData.set(MAX_HEALTH, tag.getFloat("MaxHealth"));
        this.entityData.set(IS_SPAWNING_ANIMATION, tag.getBoolean("IsSpawningAnimation"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("ConfigInitialized", this.configInitialized);
        tag.putString("OrbConfig", OrbConfigManager.configToJson(config));
        if (barrierEntityUUID != null) { tag.putUUID("BarrierUUID", barrierEntityUUID); }
        tag.putInt("SpawnTimer", spawnTimer);
        tag.putInt("MobSpawnDelayTimer", mobSpawnDelayTimer);
        tag.putBoolean("IsCurrentlySpawning", isCurrentlySpawning);
        tag.putInt("SpawnAnimationTimer", spawnAnimationTimer);
        tag.putInt("InstantSpawnDelayCounter", this.instantSpawnDelayCounter);
        tag.putInt("InstantSpawnMobsRemaining", this.instantSpawnMobsRemaining);
        tag.putInt("InstantSpawnBatchCooldown", this.instantSpawnBatchCooldown);
        tag.putInt("InstantSpawnRadiusCache", this.instantSpawnRadiusCache);
        tag.putString("InstantSpawnMobTypeCache", this.instantSpawnMobTypeCache);
        tag.putInt("SyncCounter", this.syncCounter);
        tag.putFloat("Health", getHealth());
        tag.putFloat("MaxHealth", getMaxHealth());
        tag.putBoolean("IsSpawningAnimation", this.entityData.get(IS_SPAWNING_ANIMATION));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<OrbEntity> controller = new AnimationController<>(this, "main", 0, this::animationPredicate);
        controllers.add(controller);
    }

    private PlayState animationPredicate(AnimationState<OrbEntity> state) {
        state.getController().setAnimation(RawAnimation.begin().thenLoop(getAnimationForCurrentState()));
        return PlayState.CONTINUE;
    }

    private String getAnimationForCurrentState() {
        switch (config.getMode()) {
            case IDLE:
                return config.isActive() ? "idle" : "idle_close";
            case BARRIER:
                return config.isActive() ? "barrier_open" : "barrier_close";
            case SPAWNER:
                if (config.isActive()) {
                    if (this.entityData.get(IS_SPAWNING_ANIMATION)) {
                        return "spawner_close";
                    } else {
                        return "spawner_open";
                    }
                } else {
                    return "spawner_close";
                }
            default:
                return "idle";
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
