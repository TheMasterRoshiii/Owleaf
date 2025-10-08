package com.more_owleaf.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class BarrierEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_IS_Z_ALIGNED =
            SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.BOOLEAN);
    public static final float THICKNESS = 0.25f;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUUID;

    public BarrierEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && ownerUUID != null) {
            Entity owner = ((net.minecraft.server.level.ServerLevel) level()).getEntity(ownerUUID);
            if (owner == null || owner.isRemoved()) {
                this.discard();
            }
        }
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        recalculateBoundingBox();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_IS_Z_ALIGNED.equals(key)) {
            recalculateBoundingBox();
        }
    }

    public void setOrientation(boolean isZAligned) {
        this.entityData.set(DATA_IS_Z_ALIGNED, isZAligned);
        recalculateBoundingBox();
    }

    public void recalculateBoundingBox() {
        float length = this.getDimensions(Pose.STANDING).width;
        float height = this.getDimensions(Pose.STANDING).height;
        AABB newBoundingBox;
        if (this.entityData.get(DATA_IS_Z_ALIGNED)) {
            newBoundingBox = new AABB(getX() - THICKNESS / 2, getY(), getZ() - length / 2, getX() + THICKNESS / 2, getY() + height, getZ() + length / 2);
        } else {
            newBoundingBox = new AABB(getX() - length / 2, getY(), getZ() - THICKNESS / 2, getX() + length / 2, getY() + height, getZ() + THICKNESS / 2);
        }
        this.setBoundingBox(newBoundingBox.inflate(0.00));
    }

    @Override public boolean canBeCollidedWith() { return true; }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override public boolean isPushable() { return false; }


    public void setOwner(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public UUID getOwnerUUID() { return this.ownerUUID; }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_IS_Z_ALIGNED, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) { this.ownerUUID = tag.getUUID("Owner"); }
        this.setOrientation(tag.getBoolean("IsZAligned"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) { tag.putUUID("Owner", this.ownerUUID); }
        tag.putBoolean("IsZAligned", this.entityData.get(DATA_IS_Z_ALIGNED));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}