package com.more_owleaf.entities.orb;

import com.google.gson.annotations.SerializedName;

public class OrbConfig {
    public enum OrbMode {
        IDLE, BARRIER, SPAWNER
    }

    @SerializedName("mode")
    private OrbMode mode = OrbMode.IDLE;

    @SerializedName("active")
    private boolean isActive = false;

    @SerializedName("mob_type")
    private String mobType = "minecraft:zombie";

    @SerializedName("spawn_rate")
    private int spawnRate = 100;

    @SerializedName("barrier_size")
    private float barrierSize = 1.0f;

    @SerializedName("barrier_height")
    private float barrierHeight = 3.0f;

    @SerializedName("max_mobs")
    private int maxMobs = 5;

    public OrbMode getMode() { return mode; }
    public void setMode(OrbMode mode) { this.mode = mode; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getMobType() { return mobType; }
    public void setMobType(String mobType) { this.mobType = mobType; }

    public int getSpawnRate() { return spawnRate; }
    public void setSpawnRate(int spawnRate) { this.spawnRate = spawnRate; }

    public float getBarrierSize() { return barrierSize; }
    public void setBarrierSize(float barrierSize) { this.barrierSize = barrierSize; }

    public float getBarrierHeight() { return barrierHeight; }
    public void setBarrierHeight(float barrierHeight) { this.barrierHeight = barrierHeight; }

    public int getMaxMobs() { return maxMobs; }
    public void setMaxMobs(int maxMobs) { this.maxMobs = maxMobs; }
}
