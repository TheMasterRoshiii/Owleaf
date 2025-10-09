package com.more_owleaf.config;

import com.google.gson.annotations.SerializedName;

public class OrbConfig {
    public enum OrbMode {
        IDLE, BARRIER, SPAWNER
    }

    @SerializedName("mode")
    private OrbMode mode = OrbMode.IDLE;

    @SerializedName("active")
    private boolean isActive = false;

    @SerializedName("barrier_z_aligned")
    private boolean isBarrierZAligned = false;

    @SerializedName("mob_type")
    private String mobType = "minecraft:zombie";

    @SerializedName("spawn_rate")
    private int spawnRate = 100;

    @SerializedName("max_mobs")
    private int maxMobs = 6;

    @SerializedName("spawn_count")
    private int spawnCount = 4;

    @SerializedName("spawn_radius")
    private int spawnRadius = 16;

    @SerializedName("charge_creepers")
    private boolean chargeCreepers = false;

    @SerializedName("instant_spawn_radius")
    private int instantSpawnRadius = 10;

    @SerializedName("instant_spawn_quantity")
    private int instantSpawnQuantity = 100;

    @SerializedName("instant_spawn_delay")
    private int instantSpawnDelay = 100;

    @SerializedName("max_health")
    private float maxHealth = 100.0f;

    @SerializedName("resistance")
    private float resistance = 0.0f;

    @SerializedName("is_damageable")
    private boolean isDamageable = false;

    public OrbMode getMode() { return mode; }
    public void setMode(OrbMode mode) { this.mode = mode; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isBarrierZAligned() { return isBarrierZAligned; }
    public void setBarrierZAligned(boolean barrierZAligned) { isBarrierZAligned = barrierZAligned; }

    public String getMobType() { return mobType; }
    public void setMobType(String mobType) { this.mobType = mobType; }

    public int getSpawnRate() { return spawnRate; }
    public void setSpawnRate(int spawnRate) { this.spawnRate = spawnRate; }

    public int getMaxMobs() { return maxMobs; }
    public void setMaxMobs(int maxMobs) { this.maxMobs = maxMobs; }

    public int getSpawnCount() { return spawnCount; }
    public void setSpawnCount(int spawnCount) { this.spawnCount = spawnCount; }

    public int getSpawnRadius() { return spawnRadius; }
    public void setSpawnRadius(int spawnRadius) { this.spawnRadius = spawnRadius; }

    public int getInstantSpawnRadius() { return instantSpawnRadius; }
    public void setInstantSpawnRadius(int instantSpawnRadius) { this.instantSpawnRadius = instantSpawnRadius; }

    public int getInstantSpawnQuantity() { return instantSpawnQuantity; }
    public void setInstantSpawnQuantity(int instantSpawnQuantity) { this.instantSpawnQuantity = instantSpawnQuantity; }

    public int getInstantSpawnDelay() { return instantSpawnDelay; }
    public void setInstantSpawnDelay(int instantSpawnDelay) { this.instantSpawnDelay = instantSpawnDelay; }

    public boolean getChargeCreepers() { return chargeCreepers; }
    public void setChargeCreepers(boolean chargeCreepers) { this.chargeCreepers = chargeCreepers; }

    public float getMaxHealth() { return maxHealth; }
    public void setMaxHealth(float maxHealth) { this.maxHealth = maxHealth; }

    public float getResistance() { return resistance; }
    public void setResistance(float resistance) { this.resistance = resistance; }

    public boolean isDamageable() { return isDamageable; }
    public void setDamageable(boolean damageable) { this.isDamageable = damageable; }
}
